package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import com.amenicsystem.application.pagamento.usecase.ConfirmarPagamentoPorWebhookUseCase;
import com.amenicsystem.application.port.out.ConsultaPagamentoResult;
import com.amenicsystem.application.port.out.PagamentoGatewayPort;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Job de reconciliação de pagamentos pendentes — safety net essencial.
 *
 * <h3>Motivação:</h3>
 * <p>Um sistema de pagamentos que depende exclusivamente de webhooks tem uma falha
 * de design. Webhooks podem falhar silenciosamente, chegar fora de ordem, ou o
 * endpoint pode estar temporariamente indisponível. Este job garante que pagamentos
 * pendentes "esquecidos" sejam reconciliados periodicamente.</p>
 *
 * <h3>Execução:</h3>
 * <p>A cada 5 minutos, busca pagamentos com status PENDENTE criados há mais de
 * 15 minutos (threshold configurável). Para cada um, consulta a API do MP e
 * aplica a transição se o status mudou.</p>
 *
 * <h3>Idempotência:</h3>
 * <p>O processamento de cada pagamento passa pelo {@link WebhookProcessingService},
 * que tem toda a lógica de deduplicação e locking. O job é safe para re-execução.</p>
 *
 * <h3>Resiliência:</h3>
 * <p>Erros em um pagamento específico não interrompem o loop dos demais.
 * Cada falha é logada individualmente com contexto detalhado.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookReconciliationJob {

    /** Pagamentos PENDENTES mais antigos que este threshold são reconciliados. */
    private static final int THRESHOLD_MINUTOS = 15;

    private final PagamentoRepository pagamentoRepository;
    private final PagamentoGatewayPort pagamentoGateway;
    private final ConfirmarPagamentoPorWebhookUseCase confirmarPagamentoUseCase;

    /**
     * Reconcilia pagamentos pendentes a cada 5 minutos.
     * Delay inicial de 2 minutos para aguardar startup completo.
     */
    @Scheduled(fixedDelay = 5, initialDelay = 2, timeUnit = TimeUnit.MINUTES)
    public void reconciliar() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(THRESHOLD_MINUTOS);
        List<Pagamento> pendentes = pagamentoRepository.findPendentesAntigos(threshold);

        if (pendentes.isEmpty()) {
            log.debug("[RECONCILIATION] Nenhum pagamento pendente para reconciliar.");
            return;
        }

        log.info("[RECONCILIATION_START] Iniciando reconciliação de {} pagamento(s) pendente(s) " +
                "(criados antes de {})", pendentes.size(), threshold);

        int reconciliados = 0;
        int semAlteracao = 0;
        int erros = 0;

        for (Pagamento pagamento : pendentes) {
            try {
                boolean atualizado = reconciliarPagamento(pagamento);
                if (atualizado) reconciliados++;
                else semAlteracao++;
            } catch (Exception e) {
                erros++;
                log.error("[RECONCILIATION_ERROR] Falha ao reconciliar pagamento — " +
                                "pagamentoId={}, paymentId={}, erro={}",
                        pagamento.getId().id(),
                        pagamento.getPaymentId() != null ? pagamento.getPaymentId() : "null",
                        e.getMessage(), e);
            }
        }

        log.info("[RECONCILIATION_DONE] Reconciliação concluída — reconciliados={}, semAlteracao={}, erros={}",
                reconciliados, semAlteracao, erros);
    }

    /**
     * Reconcilia um pagamento individual.
     *
     * @return true se o status foi atualizado, false se MP confirma PENDENTE ou status igual
     */
    private boolean reconciliarPagamento(Pagamento pagamento) {
        // paymentId é o ID numérico real do pagamento no Mercado Pago.
        // transacaoExternaId é o ID da Preference (formato sellerId-UUID) — NÃO consultável via PaymentClient.
        // Se paymentId for nulo, o webhook ainda não chegou: não há como consultar o MP.
        String paymentIdParaConsulta = pagamento.getPaymentId();

        if (paymentIdParaConsulta == null) {
            log.debug("[RECONCILIATION_SKIP] Pagamento sem paymentId (webhook ainda não recebido) — " +
                    "pagamentoId={}, transacaoExternaId={}",
                    pagamento.getId().id(), pagamento.getTransacaoExternaId());
            return false;
        }

        MDC.put("paymentId", paymentIdParaConsulta);
        try {
            ConsultaPagamentoResult resultado = pagamentoGateway.consultarStatusPagamento(paymentIdParaConsulta);

            // Se MP confirma PENDENTE, ainda aguardando — sem ação
            if ("pending".equalsIgnoreCase(resultado.status())) {
                log.debug("[RECONCILIATION_PENDING] MP confirma PENDENTE — pagamentoId={}, paymentId={}",
                        pagamento.getId().id(), paymentIdParaConsulta);
                return false;
            }

            // Status mudou — processar como webhook
            log.info("[RECONCILIATION_UPDATE] Status diferente detectado — pagamentoId={}, " +
                            "paymentId={}, statusMP={}",
                    pagamento.getId().id(), paymentIdParaConsulta, resultado.status());

            var command = new WebhookPagamentoCommand(
                    paymentIdParaConsulta,
                    resultado.status(),
                    resultado.externalReference(),
                    "reconciliation-job"  // notificationId sintético para tracing
            );

            confirmarPagamentoUseCase.execute(command);
            return true;

        } finally {
            MDC.remove("paymentId");
        }
    }
}
