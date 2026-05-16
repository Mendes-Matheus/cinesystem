package com.amenicsystem.interfaces.http.pagamento.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO aninhado representando o campo {@code data} do payload do webhook do Mercado Pago.
 *
 * <p>Contém o ID do recurso afetado (ex: ID do pagamento para eventos do tipo "payment").</p>
 *
 * <p>Exemplo de payload do MP:</p>
 * <pre>
 * {
 *   "data": { "id": "999999999" }
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookDataDTO(
        /** ID do recurso afetado (ex: payment ID). Pode ser String numérico ou alfanumérico. */
        String id
) {}
