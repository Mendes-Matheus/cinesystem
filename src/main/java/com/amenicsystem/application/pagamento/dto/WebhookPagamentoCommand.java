package com.amenicsystem.application.pagamento.dto;

/**
 * Command gerado a partir da notificação do Mercado Pago após validação HMAC
 * e consulta server-to-server à API do MP.
 *
 * <h3>Campos:</h3>
 * <ul>
 *   <li>{@code paymentId} — ID numérico do pagamento efetivado (data.id do webhook)</li>
 *   <li>{@code statusMercadoPago} — status consultado diretamente na API do MP (nunca do payload)</li>
 *   <li>{@code externalReference} — referência externa retornada pela API do MP (ex: "ingresso-49")</li>
 *   <li>{@code notificationId} — ID único da notificação — apenas para logs e tracing</li>
 * </ul>
 *
 * <h3>Correlação:</h3>
 * <p>O {@code paymentId} é a identidade canônica. O {@code externalReference} é usado apenas
 * como fallback one-time para correlacionar pagamentos antigos sem {@code payment_id} vinculado.</p>
 *
 * <h3>Segurança:</h3>
 * <p>Todos os dados vêm da API do MP (server-to-server), não do payload do webhook.
 * O orchestrator nunca propaga dados do payload sem validação.</p>
 *
 * @param paymentId         ID numérico do pagamento (data.id do webhook)
 * @param statusMercadoPago status consultado na API do MP
 * @param externalReference referência externa retornada pela API do MP
 * @param notificationId    ID da notificação — apenas para logs/tracing, não é idempotency key
 */
public record WebhookPagamentoCommand(
        String paymentId,
        String statusMercadoPago,
        String externalReference,
        String notificationId
) {}
