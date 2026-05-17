package com.amenicsystem.infrastructure.payment.mercadopago;

import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import com.amenicsystem.application.pagamento.usecase.ConfirmarPagamentoPorWebhookUseCase;
import com.amenicsystem.application.port.out.ConsultaPagamentoResult;
import com.amenicsystem.application.port.out.PagamentoGatewayPort;
import com.amenicsystem.domain.shared.DuplicateWebhookException;
import com.amenicsystem.domain.shared.InvalidPaymentTransitionException;
import com.amenicsystem.domain.shared.WebhookException;
import com.amenicsystem.domain.shared.WebhookValidationException;
import com.amenicsystem.interfaces.http.pagamento.dto.MercadoPagoWebhookPayloadDTO;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Orquestrador de infraestrutura para webhooks do Mercado Pago.
 *
 * <p>Coordena exclusivamente componentes de infraestrutura. Não conhece regras de domínio,
 * ingressoId, externalReference como identidade, nem FSM de pagamento.</p>
 *
 * <h3>Fluxo:</h3>
 * <ol>
 *   <li>Validar assinatura HMAC → {@link MercadoPagoWebhookSignatureValidator}</li>
 *   <li>Parsear e filtrar evento → {@link MercadoPagoWebhookParser}</li>
 *   <li>Consultar status real na API do MP → {@link PagamentoGatewayPort}</li>
 *   <li>Montar command e delegar → {@link ConfirmarPagamentoPorWebhookUseCase}</li>
 * </ol>
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookOrchestrator {

    private final MercadoPagoWebhookSignatureValidator signatureValidator;
    private final MercadoPagoWebhookParser parser;
    private final PagamentoGatewayPort pagamentoGateway;
    private final ConfirmarPagamentoPorWebhookUseCase confirmarPagamentoUseCase;

    /**
     * Processa uma notificação webhook do Mercado Pago de ponta a ponta.
     * Nunca lança exceção — o controller sempre retorna HTTP 200.
     */
    public void processar(
            MercadoPagoWebhookPayloadDTO payload,
            String typeParam,
            String dataIdParam,
            String xSignature,
            String xRequestId) {

        String notificationId = payload != null ? payload.getNotificationId() : "unknown";
        configurarMDC(xRequestId, notificationId);

        try {
            log.info("[WEBHOOK_RECEIVED] Webhook MP recebido — notificationId={}, type={}, dataId={}, action={}",
                    notificationId,
                    payload != null ? payload.getNotificationType() : typeParam,
                    dataIdParam != null ? dataIdParam : (payload != null ? payload.getPaymentId() : "null"),
                    payload != null ? payload.action() : "null");

            // ── 1. Validar assinatura HMAC ──
            String dataIdParaValidacao = dataIdParam != null ? dataIdParam
                    : (payload != null ? payload.getPaymentId() : null);

            // Lança WebhookValidationException (non-retryable) se inválida
            signatureValidator.validar(xSignature, xRequestId, dataIdParaValidacao);

            // ── 2. Parsear e filtrar evento ──
            var parsedOpt = parser.parsear(payload, typeParam, dataIdParam);
            if (parsedOpt.isEmpty()) {
                // Evento filtrado (não é payment, ou IPN legacy sem suporte) — já logado no parser
                return;
            }

            var parsed = parsedOpt.get();
            String paymentId = parsed.paymentId();
            MDC.put("paymentId", paymentId);

            log.info("[WEBHOOK_PROCESSING] Consultando API do MP — paymentId={}, notificationId={}",
                    paymentId, notificationId);

            // ── 3. Consultar status real na API do MP (nunca confiar no payload) ──
            ConsultaPagamentoResult resultado = pagamentoGateway.consultarStatusPagamento(paymentId);

            log.info("[WEBHOOK_CONSULTED] Status obtido da API MP — paymentId={}, status={}, externalRef={}",
                    paymentId, resultado.status(), resultado.externalReference());

            // ── 4. Montar command e delegar ao use case ──
            var command = new WebhookPagamentoCommand(
                    paymentId,
                    resultado.status(),
                    resultado.externalReference(),
                    notificationId
            );

            confirmarPagamentoUseCase.execute(command);

            log.info("[WEBHOOK_PROCESSED] Webhook processado — paymentId={}, status={}, notificationId={}",
                    paymentId, resultado.status(), notificationId);

        } catch (OptimisticLockException e) {
            // Retryable: outro thread salvou a entidade entre o load e o save.
            // O SELECT FOR UPDATE serializa webhooks no path principal, mas código fora
            // do fluxo (ex: admin, CancelarIngresso) pode incrementar @Version concorrentemente.
            // O MP retentará e o próximo lock garantirá consistência.
            log.warn("[WEBHOOK_OPTIMISTIC_LOCK] Conflito de versão ao salvar pagamento — " +
                    "notificationId={}, MP retentará automaticamente", notificationId);

        } catch (DuplicateWebhookException e) {
            // Non-retryable — já processado com sucesso anteriormente
            log.info("[WEBHOOK_DUPLICATE] {} — notificationId={}", e.getMessage(), notificationId);

        } catch (WebhookValidationException e) {
            // Non-retryable — assinatura forjada ou malformada
            log.warn("[WEBHOOK_SIGNATURE_INVALID] {} — notificationId={}", e.getMessage(), notificationId);

        } catch (InvalidPaymentTransitionException e) {
            // Non-retryable — FSM rejeitou transição (estado atual não permite)
            log.warn("[WEBHOOK_INVALID_TRANSITION] {} — notificationId={}, paymentId={}",
                    e.getMessage(), notificationId, e.getStatusAtual());

        } catch (WebhookException e) {
            // Outras WebhookException (non-retryable por padrão)
            log.warn("[WEBHOOK_NON_RETRYABLE] {} — notificationId={}", e.getMessage(), notificationId);

        } catch (Exception e) {
            // Retryable ou desconhecido — MP retentará automaticamente
            // Log ERROR com stack trace para alertar o time de operações
            log.error("[WEBHOOK_ERROR] Erro ao processar webhook — notificationId={}, tipo={}, mensagem={}",
                    notificationId, e.getClass().getSimpleName(), e.getMessage(), e);

        } finally {
            limparMDC();
        }
    }

    private void configurarMDC(String requestId, String notificationId) {
        if (requestId != null && !requestId.isBlank()) MDC.put("requestId", requestId);
        if (notificationId != null) MDC.put("notificationId", notificationId);
    }

    private void limparMDC() {
        MDC.remove("requestId");
        MDC.remove("notificationId");
        MDC.remove("paymentId");
    }
}
