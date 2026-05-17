package com.amenicsystem.interfaces.http.pagamento;

import com.amenicsystem.infrastructure.payment.mercadopago.MercadoPagoWebhookOrchestrator;
import com.amenicsystem.interfaces.http.pagamento.dto.MercadoPagoWebhookPayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para receber notificações IPN/Webhook do Mercado Pago.
 * @see MercadoPagoWebhookOrchestrator
 */
@RestController
@RequestMapping("/api/v1/mercadopago")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookController {

    private final MercadoPagoWebhookOrchestrator orchestrator;

    /**
     * Recebe notificação webhook do Mercado Pago.
     *
     * <p>O MP envia dois formatos de notificação:</p>
     * <ul>
     *   <li><strong>Webhooks V2:</strong> body com {@code type}, {@code data.id}, {@code action};
     *       query params {@code type} e {@code data.id}; headers {@code x-signature} e {@code x-request-id}</li>
     *   <li><strong>IPN legacy:</strong> body com {@code topic} e {@code resource}
     *       (sem headers de assinatura)</li>
     * </ul>
     *
     * @param xSignature  header de assinatura HMAC do MP (pode ser null para IPN legacy)
     * @param xRequestId  header de rastreabilidade do MP (pode ser null para IPN legacy)
     * @param typeParam   query param {@code type} (fallback para extração do body)
     * @param dataIdParam query param {@code data.id} (fallback para extração do body)
     * @param payload     corpo da requisição deserializado em DTO tipado
     * @return HTTP 200 sempre
     */
    @PostMapping("/notification")
    public ResponseEntity<Void> receberNotificacao(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestBody(required = false) MercadoPagoWebhookPayloadDTO payload) {

        orchestrator.processar(payload, typeParam, dataIdParam, xSignature, xRequestId);

        return ResponseEntity.ok().build();
    }
}
