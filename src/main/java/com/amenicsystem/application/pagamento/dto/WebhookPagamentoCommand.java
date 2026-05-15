package com.amenicsystem.application.pagamento.dto;

/**
 * Command gerado a partir da notificação do Mercado Pago.
 *
 * IMPORTANTE: {@code paymentId} é o ID do PAGAMENTO (não da preference).
 * O webhook do MP envia data.id com o ID numérico do pagamento efetivado.
 * O status é consultado na API do MP usando esse ID — nunca confiamos no payload recebido.
 */
public record WebhookPagamentoCommand(
        /** ID numérico do pagamento (data.id do payload do webhook). */
        String paymentId,

        /** Status consultado diretamente na API do MP — não o do payload. */
        String statusMercadoPago,

        /** ID do ingresso associado ao pagamento. */
        Long ingressoId
) {}
