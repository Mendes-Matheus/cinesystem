package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.outbox.IngressoCompradoPayload;
import com.amenicsystem.application.outbox.OutboxEvent;
import com.amenicsystem.application.outbox.OutboxRepository;
import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import com.amenicsystem.application.port.out.ReservaAssentoPort;
import com.amenicsystem.domain.assento.Assento;
import com.amenicsystem.domain.assento.AssentoRepository;
import com.amenicsystem.domain.filme.Filme;
import com.amenicsystem.domain.filme.FilmeRepository;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import com.amenicsystem.domain.pagamento.StatusPagamento;
import com.amenicsystem.domain.sessao.Sessao;
import com.amenicsystem.domain.sessao.SessaoAssento;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amenicsystem.application.port.out.*;
import com.amenicsystem.domain.ingresso.StatusIngresso;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmarPagamentoPorWebhookUseCaseImpl implements ConfirmarPagamentoPorWebhookUseCase {

    private final PagamentoRepository pagamentoRepository;
    private final IngressoRepository ingressoRepository;
    private final SessaoRepository sessaoRepository;
    private final OutboxRepository outboxRepository;
    private final ReservaAssentoPort reservaPort;
    private final UsuarioRepository usuarioRepository;
    private final AssentoRepository assentoRepository;
    private final FilmeRepository filmeRepository;

    /**
     * Mapeamento dos status brutos do Mercado Pago para o domínio do CineSystem.
     *
     * Status MP → StatusPagamento:
     *   approved     → APROVADO
     *   pending      → PENDENTE
     *   authorized   → AUTORIZADO
     *   in_process   → EM_PROCESSO
     *   in_mediation → EM_MEDIACAO
     *   rejected     → REJEITADO
     *   cancelled    → CANCELADO
     *   refunded     → REEMBOLSADO
     *   charged_back → CONTESTADO
     *   (outros)     → DESCONHECIDO
     */
    private StatusPagamento mapearStatus(String statusMercadoPago) {
        if (statusMercadoPago == null) return StatusPagamento.DESCONHECIDO;
        return switch (statusMercadoPago.toLowerCase()) {
            case "approved"     -> StatusPagamento.APROVADO;
            case "pending"      -> StatusPagamento.PENDENTE;
            case "authorized"   -> StatusPagamento.AUTORIZADO;
            case "in_process"   -> StatusPagamento.EM_PROCESSO;
            case "in_mediation" -> StatusPagamento.EM_MEDIACAO;
            case "rejected"     -> StatusPagamento.REJEITADO;
            case "cancelled"    -> StatusPagamento.CANCELADO;
            case "refunded"     -> StatusPagamento.REEMBOLSADO;
            case "charged_back" -> StatusPagamento.CONTESTADO;
            default -> {
                log.warn("Status desconhecido recebido do Mercado Pago: {}", statusMercadoPago);
                yield StatusPagamento.DESCONHECIDO;
            }
        };
    }

    @Override
    @Transactional
    public void execute(WebhookPagamentoCommand command) {
        log.info("Processando webhook MP — paymentId={}, status={}",
                command.paymentId(), command.statusMercadoPago());

        if (command.ingressoId() == null) {
            log.warn("IngressoId não encontrado no externalReference para paymentId={}", command.paymentId());
            return;
        }

        var pagamentoOpt = pagamentoRepository.findByIngressoId(
                new com.amenicsystem.domain.ingresso.IngressoId(command.ingressoId()));
        if (pagamentoOpt.isEmpty()) {
            log.warn("Pagamento não encontrado para ingressoId={}. " +
                            "Pode ser notificação de outro sistema ou preference ainda não confirmada.",
                    command.ingressoId());
            return;
        }

        var pagamento = pagamentoOpt.get();

        // Idempotência: já processado
        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            log.info("Pagamento {} já processado (status={}). Ignorando notificação duplicada.",
                    command.paymentId(), pagamento.getStatus());
            return;
        }

        StatusPagamento novoStatus = mapearStatus(command.statusMercadoPago());
        var ingresso = ingressoRepository.findById(pagamento.getIngressoId())
                .orElseThrow(() -> new IllegalStateException(
                        "Ingresso não encontrado para pagamento " + command.paymentId()));

        switch (novoStatus) {
            case APROVADO -> {
                ingresso.ativar();
                ingressoRepository.save(ingresso);
                pagamento.aprovar();
                pagamentoRepository.save(pagamento);

                var sessaoAssento = sessaoRepository
                        .findSessaoAssentoById(ingresso.getSessaoAssentoId())
                        .orElse(null);

                var payload = construirPayloadIngressoComprado(ingresso, sessaoAssento);
                outboxRepository.save(OutboxEvent.of(
                        "IngressoComprado", ingresso.getId().id().toString(), payload));

                log.info("Ingresso {} ativado após pagamento aprovado.", ingresso.getId().id());
            }

            case REJEITADO, CANCELADO -> {
                ingresso.cancelar();
                ingressoRepository.save(ingresso);

                if (novoStatus == StatusPagamento.REJEITADO) {
                    pagamento.rejeitar();
                } else {
                    pagamento.cancelar();
                }
                pagamentoRepository.save(pagamento);

                sessaoRepository.findSessaoAssentoById(ingresso.getSessaoAssentoId())
                        .ifPresent(sa -> reservaPort.liberar(sa.getSessaoId(), sa.getAssentoId()));

                log.info("Ingresso {} cancelado. Status MP: {}", ingresso.getId().id(), novoStatus);
            }

            // Transições intermediárias — aguardar nova notificação
            case PENDENTE, AUTORIZADO, EM_PROCESSO, EM_MEDIACAO -> {
                log.info("Pagamento {} em status intermediário: {}. Aguardando confirmação final.",
                        command.paymentId(), novoStatus);
            }

            // Pós-aprovação: reembolso ou chargeback — cancelar ingresso se ainda ativo
            case REEMBOLSADO, CONTESTADO -> {
                if (ingresso.getStatus() == StatusIngresso.ATIVO) {
                    ingresso.cancelar();
                    ingressoRepository.save(ingresso);
                }
                pagamento.cancelar();
                pagamentoRepository.save(pagamento);
                log.info("Ingresso {} cancelado por {} após aprovação.", ingresso.getId().id(), novoStatus);
            }

            case DESCONHECIDO -> log.warn(
                    "Status desconhecido para paymentId={}. Nenhuma ação tomada.", command.paymentId());
        }
    }

    private IngressoCompradoPayload construirPayloadIngressoComprado(
            Ingresso ingresso, SessaoAssento sessaoAssento) {

        // Buscar e-mail do usuário
        String emailUsuario = null;
        if (ingresso.getUsuarioId() != null) {
            emailUsuario = usuarioRepository.findById(ingresso.getUsuarioId())
                    .map(u -> u.getEmail().valor())
                    .orElse(null);
        }

        // Buscar dados do assento, sessão e filme
        String fileira = null;
        int numeroAssento = 0;
        String tituloFilme = null;
        LocalDateTime dataHora = null;

        if (sessaoAssento != null) {
            // Assento → fileira e número
            Assento assento = assentoRepository.findById(sessaoAssento.getAssentoId()).orElse(null);
            if (assento != null) {
                fileira = assento.getFileira();
                numeroAssento = assento.getNumero();
            }

            // Sessão → dataHora; Filme → título
            Sessao sessao = sessaoRepository.findById(sessaoAssento.getSessaoId()).orElse(null);
            if (sessao != null) {
                dataHora = sessao.getDataHora();
                Filme filme = filmeRepository.findById(sessao.getFilmeId()).orElse(null);
                if (filme != null) {
                    tituloFilme = filme.getTitulo();
                }
            }
        }

        return new IngressoCompradoPayload(
                ingresso.getId().id(),
                ingresso.getCodigo().valor(),   // String, não CodigoIngresso
                emailUsuario,
                tituloFilme,
                dataHora,
                fileira,
                numeroAssento,
                ingresso.getValorPago()
        );
    }
}
