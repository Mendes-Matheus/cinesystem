package com.amenicsystem.infrastructure.payment.mercadopago;

import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import com.amenicsystem.application.pagamento.usecase.ConfirmarPagamentoPorWebhookUseCase;
import com.amenicsystem.application.port.out.ConsultaPagamentoResult;
import com.amenicsystem.application.port.out.PagamentoGatewayPort;
import com.amenicsystem.interfaces.http.pagamento.dto.MercadoPagoWebhookPayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Orquestrador do fluxo de processamento de webhooks do Mercado Pago.
 *
 * <h3>Posição na Arquitetura:</h3>
 * <p>Reside na camada de <strong>infraestrutura</strong> porque coordena componentes de
 * infraestrutura (validator, parser, gateway) antes de delegar a regra de negócio
 * ao use case na camada de aplicação. O controller chama apenas este componente,
 * mantendo-se extremamente fino.</p>
 *
 * <h3>Fluxo Completo:</h3>
 * <ol>
 *   <li><strong>Validar assinatura HMAC</strong> — rejeita notificações forjadas</li>
 *   <li><strong>Parsear e filtrar</strong> — extrai dados e ignora eventos não relevantes</li>
 *   <li><strong>Consultar status real</strong> — server-to-server via API do MP (nunca confia no payload)</li>
 *   <li><strong>Resolver ingressoId</strong> — extrai do externalReference via parser estruturado</li>
 *   <li><strong>Delegar ao use case</strong> — toda regra de negócio fica no application layer</li>
 * </ol>
 *
 * <h3>Observabilidade:</h3>
 * <p>Usa MDC (Mapped Diagnostic Context) para enriquecer todos os logs com
 * {@code paymentId}, {@code ingressoId}, {@code requestId} e {@code notificationId}.
 * Logs estruturados com prefixos de categoria para filtragem rápida:</p>
 * <ul>
 *   <li>{@code [WEBHOOK_RECEIVED]} — notificação chegou</li>
 *   <li>{@code [WEBHOOK_SIGNATURE_INVALID]} — falha de autenticação</li>
 *   <li>{@code [WEBHOOK_IGNORED]} — tipo não processável</li>
 *   <li>{@code [WEBHOOK_PROCESSED]} — sucesso</li>
 *   <li>{@code [WEBHOOK_ERROR]} — erro durante processamento</li>
 * </ul>
 *
 * <h3>Segurança:</h3>
 * <ul>
 *   <li>Validação HMAC-SHA256 com constant-time comparison</li>
 *   <li>Consulta server-to-server — nunca confia no payload do webhook</li>
 *   <li>Endpoint público (sem JWT) — MP não suporta Bearer</li>
 *   <li>Sempre retorna sem exceção — o controller garante HTTP 200</li>
 * </ul>
 *
 * <h3>Resiliência:</h3>
 * <p>O método {@link #processar} nunca lança exceção. Todos os erros são logados
 * e absorvidos internamente. Isso garante que o controller sempre retorne HTTP 200,
 * evitando retries infinitos do Mercado Pago.</p>
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
     *
     * <p>Este método <strong>nunca lança exceção</strong>. Todos os cenários de erro
     * são logados e tratados internamente, garantindo que o controller sempre
     * retorne HTTP 200 ao Mercado Pago.</p>
     *
     * @param payload     DTO tipado do corpo da requisição (pode ser null para IPN legacy)
     * @param typeParam   query param {@code type} (pode ser null)
     * @param dataIdParam query param {@code data.id} (pode ser null)
     * @param xSignature  header {@code x-signature} para validação HMAC (pode ser null)
     * @param xRequestId  header {@code x-request-id} para rastreabilidade (pode ser null)
     */
    public void processar(
            MercadoPagoWebhookPayloadDTO payload,
            String typeParam,
            String dataIdParam,
            String xSignature,
            String xRequestId) {

        String notificationId = payload != null ? payload.getNotificationId() : "unknown";
        configurarMDC(xRequestId, notificationId, null, null);

        try {
            log.info("[WEBHOOK_RECEIVED] Webhook MP recebido — notificationId={}, type={}, dataId={}, action={}",
                    notificationId,
                    payload != null ? payload.getNotificationType() : typeParam,
                    dataIdParam != null ? dataIdParam : (payload != null ? payload.getPaymentId() : "null"),
                    payload != null ? payload.action() : "null");

            // ── 1. Validar assinatura HMAC ──
            String dataIdParaValidacao = dataIdParam != null ? dataIdParam
                    : (payload != null ? payload.getPaymentId() : null);

            if (!signatureValidator.validar(xSignature, xRequestId, dataIdParaValidacao)) {
                log.warn("[WEBHOOK_SIGNATURE_INVALID] Notificação com assinatura inválida rejeitada — "
                        + "notificationId={}, dataId={}, requestId={}",
                        notificationId, dataIdParaValidacao, xRequestId);
                return;
            }

            // ── 2. Parsear e filtrar ──
            var parsedOpt = parser.parsear(payload, typeParam, dataIdParam);
            if (parsedOpt.isEmpty()) {
                log.debug("[WEBHOOK_IGNORED] Notificação filtrada pelo parser — notificationId={}", notificationId);
                return;
            }

            var parsed = parsedOpt.get();
            String paymentId = parsed.paymentId();
            MDC.put("paymentId", paymentId);

            log.info("[WEBHOOK_PROCESSING] Processando pagamento — paymentId={}, notificationId={}",
                    paymentId, notificationId);

            // ── 3. Consultar status real na API do MP (nunca confiar no payload) ──
            ConsultaPagamentoResult resultado = pagamentoGateway.consultarStatusPagamento(paymentId);

            log.info("[WEBHOOK_CONSULTED] Status consultado na API do MP — paymentId={}, status={}, externalReference={}",
                    paymentId, resultado.status(), resultado.externalReference());

            // ── 4. Resolver ingressoId a partir do externalReference ──
            Long ingressoId = parser.extrairIngressoId(resultado.externalReference());
            MDC.put("ingressoId", ingressoId != null ? ingressoId.toString() : "null");

            if (ingressoId == null) {
                log.warn("[WEBHOOK_ERROR] Impossível determinar ingressoId a partir de externalReference='{}' "
                        + "— pagamento não será processado. paymentId={}, notificationId={}",
                        resultado.externalReference(), paymentId, notificationId);
                return;
            }

            // ── 5. Montar command e delegar ao use case ──
            var command = new WebhookPagamentoCommand(
                    paymentId,
                    resultado.status(),
                    ingressoId,
                    notificationId
            );

            confirmarPagamentoUseCase.execute(command);

            log.info("[WEBHOOK_PROCESSED] Webhook processado com sucesso — paymentId={}, ingressoId={}, "
                    + "status={}, notificationId={}",
                    paymentId, ingressoId, resultado.status(), notificationId);

        } catch (Exception e) {
            // Absorver TODAS as exceções — o controller SEMPRE retorna 200
            log.error("[WEBHOOK_ERROR] Erro ao processar webhook do Mercado Pago — "
                    + "notificationId={}, erro={}", notificationId, e.getMessage(), e);
        } finally {
            limparMDC();
        }
    }

    /** Configura o MDC com dados de rastreabilidade. */
    private void configurarMDC(String requestId, String notificationId,
                                String paymentId, String ingressoId) {
        if (requestId != null) MDC.put("requestId", requestId);
        if (notificationId != null) MDC.put("notificationId", notificationId);
        if (paymentId != null) MDC.put("paymentId", paymentId);
        if (ingressoId != null) MDC.put("ingressoId", ingressoId);
    }

    /** Limpa o MDC ao final do processamento para não vazar entre threads. */
    private void limparMDC() {
        MDC.remove("requestId");
        MDC.remove("notificationId");
        MDC.remove("paymentId");
        MDC.remove("ingressoId");
    }
}
