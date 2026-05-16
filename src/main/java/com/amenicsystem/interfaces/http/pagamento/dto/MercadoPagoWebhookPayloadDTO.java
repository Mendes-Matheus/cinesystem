package com.amenicsystem.interfaces.http.pagamento.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO tipado para o payload completo de uma notificação Webhook do Mercado Pago.
 *
 * <p>Substitui {@code Map<String, Object>} por um record imutável com campos
 * explícitos, eliminando casts inseguros e melhorando a legibilidade.</p>
 *
 * <p>Exemplo de payload recebido do MP (tópico "payment"):</p>
 * <pre>
 * {
 *   "id": 12345,
 *   "live_mode": true,
 *   "type": "payment",
 *   "date_created": "2015-03-25T10:04:58.396-04:00",
 *   "user_id": 44444,
 *   "api_version": "v1",
 *   "action": "payment.created",
 *   "data": { "id": "999999999" }
 * }
 * </pre>
 *
 * <p>O MP também envia notificações IPN legacy com formato diferente
 * ({@code resource}, {@code topic}). Esse DTO ignora campos desconhecidos
 * para compatibilidade — a filtragem é feita pelo parser.</p>
 *
 * @see MercadoPagoWebhookDataDTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookPayloadDTO(

        /** ID único da notificação (gerado pelo MP). */
        Long id,

        /** Indica se é ambiente de produção (true) ou sandbox (false). */
        @JsonProperty("live_mode")
        Boolean liveMode,

        /**
         * Tipo do recurso notificado.
         * Para pagamentos via Checkout Pro: {@code "payment"}.
         * Outros tipos possíveis: {@code "plan"}, {@code "subscription"}, etc.
         */
        String type,

        /**
         * Ação que disparou a notificação.
         * Exemplos: {@code "payment.created"}, {@code "payment.updated"}.
         */
        String action,

        /** Versão da API usada para gerar a notificação. */
        @JsonProperty("api_version")
        String apiVersion,

        /** Data/hora de criação da notificação (ISO 8601). */
        @JsonProperty("date_created")
        String dateCreated,

        /** ID do usuário vendedor no Mercado Pago. */
        @JsonProperty("user_id")
        Long userId,

        /** Dados do recurso afetado (contém o ID do pagamento). */
        MercadoPagoWebhookDataDTO data,

        // ── Campos IPN legacy (ignorados, mas mapeados para evitar erros) ──

        /** Recurso da notificação IPN legacy (ex: ID ou URL). */
        String resource,

        /** Tópico da notificação IPN legacy (ex: "payment", "merchant_order"). */
        String topic
) {

    /**
     * Retorna o ID do pagamento, priorizando o formato Webhooks V2 ({@code data.id})
     * sobre o formato IPN legacy ({@code resource}).
     *
     * @return ID do pagamento ou {@code null} se indisponível
     */
    public String getPaymentId() {
        if (data != null && data.id() != null && !data.id().isBlank()) {
            return data.id();
        }
        return null;
    }

    /**
     * Retorna o tipo da notificação, priorizando o formato Webhooks V2 ({@code type})
     * sobre o formato IPN legacy ({@code topic}).
     *
     * @return tipo da notificação ou {@code null}
     */
    public String getNotificationType() {
        if (type != null && !type.isBlank()) {
            return type;
        }
        return topic;
    }

    /**
     * Retorna o ID da notificação como String para uso em logs e rastreabilidade.
     *
     * @return ID da notificação ou "unknown"
     */
    public String getNotificationId() {
        return id != null ? id.toString() : "unknown";
    }
}
