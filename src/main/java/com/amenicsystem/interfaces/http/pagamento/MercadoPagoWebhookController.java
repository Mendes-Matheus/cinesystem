package com.amenicsystem.interfaces.http.pagamento;

import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import com.amenicsystem.application.pagamento.usecase.ConfirmarPagamentoPorWebhookUseCase;
import com.amenicsystem.application.port.out.ConsultaPagamentoResult;
import com.amenicsystem.application.port.out.PagamentoGatewayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para receber notificações IPN/Webhook do Mercado Pago.
 *
 * Fluxo:
 *   1. O MP envia um POST com { type, data.id } para a notificationUrl configurada.
 *   2. Filtramos apenas eventos do tipo "payment".
 *   3. Consultamos o status real do pagamento na API do MP (nunca confiamos no payload).
 *   4. Extraímos o ingressoId do externalReference (formato: "ingresso-{id}").
 *   5. Delegamos ao use case de confirmação.
 *
 * Segurança:
 *   - O endpoint é público (sem JWT) pois o MP não autentica via Bearer.
 *   - A segurança é garantida pela consulta direta à API do MP (server-to-server).
 */
@RestController
@RequestMapping("/api/v1/mercadopago")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookController {

    private final PagamentoGatewayPort pagamentoGateway;
    private final ConfirmarPagamentoPorWebhookUseCase confirmarPagamentoUseCase;

    /**
     * Recebe notificação IPN do Mercado Pago.
     *
     * O MP envia um JSON com:
     * {
     *   "action": "payment.created" | "payment.updated",
     *   "type": "payment",
     *   "data": { "id": "123456789" }
     * }
     *
     * Também pode enviar via query params: ?type=payment&data.id=123456789
     *
     * Sempre retorna HTTP 200 para que o MP não reenvie a notificação indefinidamente.
     */
    @PostMapping("/notification")
    public ResponseEntity<Void> receberNotificacao(
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestParam(value = "data.id", required = false) String dataIdParam,
            @RequestBody(required = false) Map<String, Object> body) {

        log.info("Webhook MP recebido — queryParams: type={}, data.id={}, body={}", typeParam, dataIdParam, body);

        try {
            // Extrair type e paymentId — o MP pode enviar via body ou query params
            String type = typeParam;
            String paymentId = dataIdParam;

            if (body != null) {
                if (type == null) {
                    type = (String) body.get("type");
                }
                if (paymentId == null) {
                    Object data = body.get("data");
                    if (data instanceof Map<?, ?> dataMap) {
                        Object id = dataMap.get("id");
                        if (id != null) {
                            paymentId = String.valueOf(id);
                        }
                    }
                }
            }

            // Filtrar — só processamos eventos do tipo "payment"
            if (!"payment".equalsIgnoreCase(type)) {
                log.info("Notificação ignorada — tipo '{}' não é 'payment'.", type);
                return ResponseEntity.ok().build();
            }

            if (paymentId == null || paymentId.isBlank()) {
                log.warn("Notificação do tipo 'payment' recebida sem paymentId.");
                return ResponseEntity.ok().build();
            }

            log.info("Processando notificação de pagamento — paymentId={}", paymentId);

            // Consultar o status real na API do MP (nunca confiar no payload)
            ConsultaPagamentoResult resultado = pagamentoGateway.consultarStatusPagamento(paymentId);

            log.info("Status consultado na API do MP — paymentId={}, status={}, externalReference={}",
                    paymentId, resultado.status(), resultado.externalReference());

            // Extrair ingressoId do externalReference (formato: "ingresso-{id}")
            Long ingressoId = extrairIngressoId(resultado.externalReference());

            // Delegar ao use case
            var command = new WebhookPagamentoCommand(paymentId, resultado.status(), ingressoId);
            confirmarPagamentoUseCase.execute(command);

            log.info("Webhook processado com sucesso — paymentId={}, ingressoId={}", paymentId, ingressoId);

        } catch (Exception e) {
            // Logar o erro mas SEMPRE retornar 200 para o MP não reenviar
            log.error("Erro ao processar webhook do Mercado Pago: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Extrai o ID numérico do ingresso a partir do externalReference.
     * Formato esperado: "ingresso-{id}" (ex: "ingresso-49" → 49).
     */
    private Long extrairIngressoId(String externalReference) {
        if (externalReference == null || !externalReference.startsWith("ingresso-")) {
            log.warn("externalReference inválido ou ausente: '{}'", externalReference);
            return null;
        }
        try {
            return Long.parseLong(externalReference.substring("ingresso-".length()));
        } catch (NumberFormatException e) {
            log.warn("Não foi possível extrair ingressoId de externalReference: '{}'", externalReference);
            return null;
        }
    }
}
