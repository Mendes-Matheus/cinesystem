package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.outbox.OutboxEvent;
import com.amenicsystem.application.outbox.OutboxRepository;
import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import com.amenicsystem.application.port.out.ProcessedWebhookRepository;
import com.amenicsystem.application.port.out.ReservaAssentoPort;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import com.amenicsystem.domain.ingresso.StatusIngresso;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import com.amenicsystem.domain.pagamento.StatusMercadoPago;
import com.amenicsystem.domain.pagamento.StatusPagamento;
import com.amenicsystem.domain.sessao.SessaoAssento;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.shared.DuplicateWebhookException;
import com.amenicsystem.domain.shared.InvalidPaymentTransitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordenador transacional do processamento de webhooks.
 *
 * <h3>Fluxo (tudo no mesmo @Transactional):</h3>
 * <ol>
 *   <li>DESCONHECIDO → early exit sem lock, sem INSERT (MP pode retentar)</li>
 *   <li>SELECT FOR UPDATE por payment_id (serializa concorrência)</li>
 *   <li>Fallback com lock por ingressoId (pagamentos antigos sem payment_id)</li>
 *   <li>vincularPaymentId via método de domínio (estado em memória + persiste no save())</li>
 *   <li>isTerminal check → early exit se já finalizado</li>
 *   <li>INSERT processed_webhooks ON CONFLICT → 0 rows = DuplicateWebhookException</li>
 *   <li>FSM: pagamento.transicionarPara(novoStatus)</li>
 *   <li>Efeitos colaterais + COMMIT atômico</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessingService {

    private final PagamentoRepository pagamentoRepository;
    private final IngressoRepository ingressoRepository;
    private final SessaoRepository sessaoRepository;
    private final OutboxRepository outboxRepository;
    private final ReservaAssentoPort reservaPort;
    private final ProcessedWebhookRepository processedWebhookRepository;
    private final IngressoCompradoPayloadAssembler payloadAssembler;

    @Transactional
    public void processar(WebhookPagamentoCommand command) {

        // ── 1. Resolver status de domínio ──
        StatusMercadoPago statusMP = StatusMercadoPago.fromStringOrNull(command.statusMercadoPago());
        StatusPagamento novoStatus = statusMP != null ? statusMP.toDominio() : StatusPagamento.DESCONHECIDO;

        // ── 2. DESCONHECIDO: exit antecipado — sem lock, sem INSERT, MP pode retentar ──
        // Não inserimos em processed_webhooks para não bloquear retentativas futuras.
        // Se o status vier a ser mapeado em versão futura, o MP retentará com sucesso.
        if (novoStatus == StatusPagamento.DESCONHECIDO) {
            log.warn("[WEBHOOK_DESCONHECIDO] Status '{}' não mapeado — ignorando sem lock. " +
                            "paymentId={}, notificationId={}",
                    command.statusMercadoPago(), command.paymentId(), command.notificationId());
            return;
        }

        // ── 3. Resolver Pagamento com lock pessimista ──
        // FIX #2: ambos os caminhos (principal e fallback) usam lock
        Pagamento pagamento = resolverPagamentoComLock(command.paymentId(), command.externalReference());

        if (pagamento == null) {
            log.warn("[WEBHOOK_UNRESOLVED] Pagamento não localizado — paymentId={}, externalRef='{}', notificationId={}",
                    command.paymentId(), command.externalReference(), command.notificationId());
            return;
        }

        // ── 4. Vincular payment_id via domínio (FIX #3) ──
        // Atualiza estado em memória — persiste no save() do passo de efeitos colaterais.
        // Evita dessincronização entre estado em memória e banco que ocorria com UPDATE direto.
        pagamento.vincularPaymentId(command.paymentId());

        // ── 5. Verificar estado terminal ──
        if (pagamento.isTerminal()) {
            log.info("[WEBHOOK_TERMINAL] Pagamento {} já está em estado terminal ({}). paymentId={}",
                    pagamento.getId().id(), pagamento.getStatus(), command.paymentId());
            return;
        }

        // ── 6. INSERT em processed_webhooks DENTRO da transação ──
        // SELECT FOR UPDATE (passo 3) serializa — INSERT ON CONFLICT garante idempotência atômica.
        boolean registrado = processedWebhookRepository.tentarRegistrar(
                command.paymentId(), novoStatus.name(), command.notificationId());

        if (!registrado) {
            throw new DuplicateWebhookException(command.paymentId(), novoStatus.name());
        }

        // ── 7. FSM: validar e aplicar transição ──
        pagamento.transicionarPara(novoStatus);

        // ── 8. Carregar ingresso e sessaoAssento UMA VEZ ──
        Ingresso ingresso = ingressoRepository.findById(pagamento.getIngressoId())
                .orElseThrow(() -> new IllegalStateException(
                        "Ingresso não encontrado para pagamentoId=" + pagamento.getId().id()));

        SessaoAssento sessaoAssento = sessaoRepository
                .findSessaoAssentoById(ingresso.getSessaoAssentoId())
                .orElse(null);

        // ── 9. Efeitos colaterais (todos no mesmo @Transactional) ──
        aplicarEfeitosColaterais(pagamento, ingresso, sessaoAssento, novoStatus, command);

        log.info("[WEBHOOK_COMMITTED] {} → {}, paymentId={}, ingressoId={}, notificationId={}",
                command.statusMercadoPago(), novoStatus,
                command.paymentId(), ingresso.getId().id(), command.notificationId());
    }

    /**
     * Resolve o Pagamento com lock pessimista em AMBOS os caminhos.
     * FIX #2: o fallback agora usa findByIngressoIdWithLock — sem janela de concorrência.
     */
    private Pagamento resolverPagamentoComLock(String paymentId, String externalReference) {
        // Caminho principal: payment_id (identidade canônica)
        var opt = pagamentoRepository.findByPaymentIdWithLock(paymentId);
        if (opt.isPresent()) {
            return opt.get();
        }

        // Fallback com lock: externalReference → ingressoId (pagamentos antigos sem payment_id)
        if (externalReference != null && externalReference.startsWith("ingresso-")) {
            try {
                Long ingressoId = Long.parseLong(externalReference.substring("ingresso-".length()));
                var fallback = pagamentoRepository.findByIngressoIdWithLock(new IngressoId(ingressoId));
                if (fallback.isPresent()) {
                    log.info("[WEBHOOK_FALLBACK_LOCK] Correlacionado via externalRef='{}' com lock. paymentId={}",
                            externalReference, paymentId);
                    return fallback.get();
                }
            } catch (NumberFormatException ignored) {
                // externalReference com formato inesperado — falha silenciosa intencional
            }
        }

        return null;
    }

    private void aplicarEfeitosColaterais(Pagamento pagamento, Ingresso ingresso,
                                           SessaoAssento sessaoAssento,
                                           StatusPagamento novoStatus,
                                           WebhookPagamentoCommand command) {
        switch (novoStatus) {

            case APROVADO -> {
                ingresso.ativar();
                ingressoRepository.save(ingresso);
                pagamentoRepository.save(pagamento); // persiste payment_id + status + version

                var payload = payloadAssembler.montar(ingresso, sessaoAssento);
                outboxRepository.save(OutboxEvent.of(
                        "IngressoComprado", ingresso.getId().id().toString(), payload));

                log.info("[WEBHOOK_APROVADO] Ingresso {} ativado. paymentId={}",
                        ingresso.getId().id(), command.paymentId());
            }

            case REJEITADO, CANCELADO -> {
                ingresso.cancelar();
                ingressoRepository.save(ingresso);
                pagamentoRepository.save(pagamento);

                if (sessaoAssento != null) {
                    reservaPort.liberar(sessaoAssento.getSessaoId(), sessaoAssento.getAssentoId());
                } else {
                    log.warn("[WEBHOOK_CANCELADO] SessaoAssento não encontrada — assento não liberado. " +
                            "ingressoId={}, paymentId={}", ingresso.getId().id(), command.paymentId());
                }

                log.info("[WEBHOOK_CANCELADO] Ingresso {} cancelado ({}). paymentId={}",
                        ingresso.getId().id(), novoStatus, command.paymentId());
            }

            case REEMBOLSADO, CONTESTADO -> {
                if (ingresso.getStatus() == StatusIngresso.ATIVO) {
                    ingresso.cancelar();
                    ingressoRepository.save(ingresso);
                }
                pagamentoRepository.save(pagamento);

                log.info("[WEBHOOK_POS_APROVACAO] {} — ingressoId={}, paymentId={}",
                        novoStatus, ingresso.getId().id(), command.paymentId());
            }

            case PENDENTE, AUTORIZADO, EM_PROCESSO, EM_MEDIACAO -> {
                pagamentoRepository.save(pagamento);
                log.info("[WEBHOOK_INTERMEDIARIO] Status intermediário: {}. paymentId={}",
                        novoStatus, command.paymentId());
            }

            default -> log.warn("[WEBHOOK_UNHANDLED] Status sem handler: {}. paymentId={}", novoStatus, command.paymentId());
        }
    }
}
