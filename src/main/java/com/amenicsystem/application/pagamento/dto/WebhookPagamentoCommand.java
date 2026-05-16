package com.amenicsystem.application.pagamento.dto;

/**
 * Command gerado a partir da notificação do Mercado Pago após validação e consulta server-to-server.
 *
 * <p><strong>IMPORTANTE:</strong> {@code paymentId} é o ID do PAGAMENTO (não da preference).
 * O webhook do MP envia {@code data.id} com o ID numérico do pagamento efetivado.
 * O status é consultado na API do MP usando esse ID — nunca confiamos no payload recebido.</p>
 *
 * <p>O {@code notificationId} é usado para rastreabilidade e idempotência reforçada,
 * permitindo detectar notificações duplicadas antes mesmo de consultar o banco.</p>
 *
 * @param paymentId         ID numérico do pagamento (data.id do payload do webhook)
 * @param statusMercadoPago status consultado diretamente na API do MP — não o do payload
 * @param ingressoId        ID do ingresso associado ao pagamento (extraído do externalReference)
 * @param notificationId    ID único da notificação do MP — usado para logs e idempotência
 */
public record WebhookPagamentoCommand(
        String paymentId,
        String statusMercadoPago,
        Long ingressoId,
        String notificationId
) {}
