package com.amenicsystem.interfaces.http.pagamento;

import com.amenicsystem.infrastructure.payment.mercadopago.MercadoPagoWebhookOrchestrator;
import com.amenicsystem.interfaces.http.pagamento.dto.MercadoPagoWebhookPayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para receber notificações IPN/Webhook do Mercado Pago.
 *
 * <h3>Responsabilidade Única:</h3>
 * <p>Este controller é <strong>extremamente fino</strong> — sua única responsabilidade é:</p>
 * <ol>
 *   <li>Receber a requisição HTTP (body, headers, query params)</li>
 *   <li>Delegar ao {@link MercadoPagoWebhookOrchestrator}</li>
 *   <li>Retornar HTTP 200 <strong>sempre</strong></li>
 * </ol>
 *
 * <p>Não contém nenhuma lógica de negócio, parsing, validação de assinatura,
 * ou extração de IDs. Toda essa responsabilidade foi delegada aos componentes
 * apropriados na camada de infraestrutura e aplicação.</p>
 *
 * <h3>Segurança:</h3>
 * <ul>
 *   <li><strong>Endpoint público</strong> — sem JWT, pois o Mercado Pago não autentica via Bearer.</li>
 *   <li><strong>Validação HMAC-SHA256</strong> — delegada ao {@code SignatureValidator} via orquestrador.</li>
 *   <li><strong>Consulta server-to-server</strong> — nunca confia no payload recebido.</li>
 * </ul>
 *
 * <h3>HTTP 200 Constante:</h3>
 * <p>O Mercado Pago reenvia notificações indefinidamente se não receber HTTP 200/201.
 * Por isso, este endpoint <strong>sempre</strong> retorna 200, mesmo para:</p>
 * <ul>
 *   <li>Assinaturas inválidas (possível ataque)</li>
 *   <li>Tipos de evento não suportados</li>
 *   <li>Erros internos de processamento</li>
 *   <li>Payloads malformados</li>
 * </ul>
 *
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
