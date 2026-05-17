package com.amenicsystem.infrastructure.payment.mercadopago;

import com.amenicsystem.interfaces.http.pagamento.dto.MercadoPagoWebhookPayloadDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Parser e filtro de notificações webhook do Mercado Pago.
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Extrair {@code paymentId} e {@code type} do DTO tipado</li>
 *   <li>Aplicar merge com query params (fallback IPN legacy)</li>
 *   <li>Filtrar eventos — apenas {@code type = "payment"} é processado</li>
 *   <li>Rejeitar IPN legacy explicitamente com log adequado</li>
 * </ul>
 *
 * <h3>IPN Legacy:</h3>
 * <p>Notificações IPN legacy chegam com {@code topic} e {@code resource}, sem
 * headers {@code x-signature} e {@code x-request-id}. Este sistema <strong>não suporta</strong>
 * o formato IPN legacy — rejeitado explicitamente com log {@code WARN} para rastreabilidade.
 * Use {@code ?source_news=webhooks} na notification-url para receber apenas Webhooks V2.</p>
 */
@Component
@Slf4j
public class MercadoPagoWebhookParser {

    private static final String TIPO_PAYMENT = "payment";

    /**
     * Dados parseados e validados de uma notificação de pagamento.
     *
     * @param paymentId      ID do pagamento no MP (numérico como String)
     * @param notificationId ID da notificação para rastreabilidade
     * @param type           tipo do evento (sempre "payment" neste ponto)
     */
    public record ParsedWebhookData(String paymentId, String notificationId, String type) {}

    /**
     * Parseia, filtra e valida o payload do webhook.
     * Retorna empty se o evento não deve ser processado.
     *
     * @param payload       DTO tipado (pode ser null para IPN puro via query params)
     * @param typeParam     query param {@code type}
     * @param dataIdParam   query param {@code data.id}
     * @return dados parseados ou empty se descartado
     */
    public Optional<ParsedWebhookData> parsear(
            MercadoPagoWebhookPayloadDTO payload,
            String typeParam,
            String dataIdParam) {

        // Detectar e rejeitar IPN legacy explicitamente
        if (isIpnLegacy(payload, typeParam)) {
            String topic = payload != null ? payload.topic() : "null";
            String resource = payload != null ? payload.resource() : "null";
            log.warn("[IPN_LEGACY_NOT_SUPPORTED] Notificação IPN legacy recebida e ignorada. " +
                            "topic='{}', resource='{}'. " +
                            "Use '?source_news=webhooks' na notification-url para receber apenas Webhooks V2. " +
                            "notificationId={}",
                    topic, resource,
                    payload != null ? payload.getNotificationId() : "unknown");
            return Optional.empty();
        }

        // Determinar type — prioridade: query param > body
        String type = resolverType(payload, typeParam);

        // Filtrar — apenas "payment" é processado
        if (!TIPO_PAYMENT.equalsIgnoreCase(type)) {
            String tipoRecebido = type != null ? type : "null";
            // DEBUG: merchant_order, plan, subscription são eventos esperados e comuns
            log.debug("[WEBHOOK_IGNORED] Tipo '{}' ignorado — apenas '{}' é processado. notificationId={}",
                    tipoRecebido, TIPO_PAYMENT,
                    payload != null ? payload.getNotificationId() : "unknown");
            return Optional.empty();
        }

        // Determinar paymentId — prioridade: query param > body
        String paymentId = resolverPaymentId(payload, dataIdParam);

        if (paymentId == null || paymentId.isBlank()) {
            log.warn("[WEBHOOK_PARSE] Notificação do tipo 'payment' sem paymentId. notificationId={}",
                    payload != null ? payload.getNotificationId() : "unknown");
            return Optional.empty();
        }

        String notificationId = payload != null ? payload.getNotificationId() : "unknown";

        log.debug("[WEBHOOK_PARSE] Dados extraídos — paymentId={}, notificationId={}, type={}",
                paymentId, notificationId, type);

        return Optional.of(new ParsedWebhookData(paymentId, notificationId, type));
    }

    /**
     * Detecta se a notificação é IPN legacy (sem suporte neste sistema).
     *
     * <p>IPN legacy características:</p>
     * <ul>
     *   <li>Tem campo {@code topic} mas não tem {@code type}</li>
     *   <li>Tem campo {@code resource} (URL ou ID do recurso)</li>
     *   <li>Não envia headers {@code x-signature} / {@code x-request-id}</li>
     * </ul>
     */
    private boolean isIpnLegacy(MercadoPagoWebhookPayloadDTO payload, String typeParam) {
        if (typeParam != null && !typeParam.isBlank()) {
            return false; // Query param type presente → Webhooks V2
        }
        if (payload == null) {
            return false;
        }
        // IPN legacy: topic presente, type ausente, resource presente
        return payload.topic() != null && !payload.topic().isBlank()
                && (payload.type() == null || payload.type().isBlank())
                && payload.resource() != null && !payload.resource().isBlank();
    }

    /** Resolve o type com prioridade: query param → body (type) → body (topic). */
    private String resolverType(MercadoPagoWebhookPayloadDTO payload, String typeParam) {
        if (typeParam != null && !typeParam.isBlank()) return typeParam;
        if (payload != null) return payload.getNotificationType();
        return null;
    }

    /** Resolve o paymentId com prioridade: query param → body (data.id). */
    private String resolverPaymentId(MercadoPagoWebhookPayloadDTO payload, String dataIdParam) {
        if (dataIdParam != null && !dataIdParam.isBlank()) return dataIdParam;
        if (payload != null) return payload.getPaymentId();
        return null;
    }
}
