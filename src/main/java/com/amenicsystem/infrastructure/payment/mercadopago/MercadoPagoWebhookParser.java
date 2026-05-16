package com.amenicsystem.infrastructure.payment.mercadopago;

import com.amenicsystem.interfaces.http.pagamento.dto.MercadoPagoWebhookPayloadDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser e filtro de notificações webhook do Mercado Pago.
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Extrair {@code paymentId} e {@code type} do DTO tipado</li>
 *   <li>Aplicar merge com query params (fallback para notificações IPN legacy)</li>
 *   <li>Filtrar eventos — apenas {@code type = "payment"} é processado</li>
 *   <li>Resolver {@code ingressoId} a partir de {@code externalReference} via parser estruturado</li>
 * </ul>
 *
 * <h3>Design — Extensibilidade:</h3>
 * <p>O formato do {@code externalReference} é parseado via regex, permitindo
 * extensão futura para múltiplos tipos (ex: {@code "assinatura-123"}, {@code "combo-456"})
 * sem alterar a interface pública.</p>
 */
@Component
@Slf4j
public class MercadoPagoWebhookParser {

    /**
     * Tipo de evento processado por este parser.
     * Outros tipos (merchant_order, plan, etc.) são ignorados.
     */
    private static final String TIPO_PAYMENT = "payment";

    /**
     * Regex para extrair o ID numérico do externalReference.
     *
     * <p>Formato esperado: {@code "ingresso-{id}"} onde {@code {id}} é um número Long.</p>
     * <p>Exemplos válidos: {@code "ingresso-49"}, {@code "ingresso-123456"}</p>
     *
     * <p>Regex com grupo nomeado para clareza e extensibilidade:</p>
     * <pre>
     *   ^ingresso-(?<id>\d+)$
     * </pre>
     */
    private static final Pattern EXTERNAL_REFERENCE_PATTERN =
            Pattern.compile("^ingresso-(?<id>\\d+)$");

    /**
     * Dados parseados e validados de uma notificação de pagamento.
     *
     * @param paymentId      ID do pagamento no MP (numérico como String)
     * @param notificationId ID da notificação para rastreabilidade
     * @param type           tipo do evento (sempre "payment" neste ponto)
     */
    public record ParsedWebhookData(
            String paymentId,
            String notificationId,
            String type
    ) {}

    /**
     * Parseia e valida o payload do webhook, aplicando merge com query params.
     *
     * <p>Retorna {@link Optional#empty()} se:</p>
     * <ul>
     *   <li>O tipo não é {@code "payment"}</li>
     *   <li>O {@code paymentId} é ausente ou inválido</li>
     *   <li>O payload é null</li>
     * </ul>
     *
     * @param payload       DTO tipado do corpo da requisição (pode ser null)
     * @param typeParam     query param {@code type} (fallback)
     * @param dataIdParam   query param {@code data.id} (fallback)
     * @return dados parseados ou empty se não deve ser processado
     */
    public Optional<ParsedWebhookData> parsear(
            MercadoPagoWebhookPayloadDTO payload,
            String typeParam,
            String dataIdParam) {

        // Determinar type — prioridade: query param > body
        String type = resolverType(payload, typeParam);

        // Filtrar — apenas "payment" é processado
        if (!TIPO_PAYMENT.equalsIgnoreCase(type)) {
            String tipoRecebido = type != null ? type : (payload != null ? payload.topic() : "null");
            log.info("[WEBHOOK_PARSE] Notificação ignorada — tipo '{}' não é '{}'. "
                    + "notificationId={}", tipoRecebido, TIPO_PAYMENT,
                    payload != null ? payload.getNotificationId() : "unknown");
            return Optional.empty();
        }

        // Determinar paymentId — prioridade: query param > body
        String paymentId = resolverPaymentId(payload, dataIdParam);

        if (paymentId == null || paymentId.isBlank()) {
            log.warn("[WEBHOOK_PARSE] Notificação do tipo 'payment' recebida sem paymentId. "
                    + "notificationId={}", payload != null ? payload.getNotificationId() : "unknown");
            return Optional.empty();
        }

        String notificationId = payload != null ? payload.getNotificationId() : "unknown";

        log.debug("[WEBHOOK_PARSE] Dados extraídos — paymentId={}, notificationId={}, type={}",
                paymentId, notificationId, type);

        return Optional.of(new ParsedWebhookData(paymentId, notificationId, type));
    }

    /**
     * Extrai o ID numérico do ingresso a partir do {@code externalReference}
     * retornado pela consulta server-to-server à API do Mercado Pago.
     *
     * <p>Usa regex estruturado em vez de manipulação frágil de strings.
     * Formato esperado: {@code "ingresso-{id}"} (ex: {@code "ingresso-49"} → {@code 49}).</p>
     *
     * <p>Extensível: novos formatos podem ser adicionados com matchers adicionais
     * sem alterar a interface pública.</p>
     *
     * @param externalReference referência externa do pagamento
     * @return ID do ingresso ou {@code null} se não for possível extrair
     */
    public Long extrairIngressoId(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            log.warn("[WEBHOOK_PARSE] externalReference ausente — impossível determinar ingressoId");
            return null;
        }

        Matcher matcher = EXTERNAL_REFERENCE_PATTERN.matcher(externalReference.trim());
        if (!matcher.matches()) {
            log.warn("[WEBHOOK_PARSE] externalReference com formato não reconhecido: '{}'. "
                    + "Formato esperado: 'ingresso-{{id}}'", externalReference);
            return null;
        }

        try {
            return Long.parseLong(matcher.group("id"));
        } catch (NumberFormatException e) {
            log.warn("[WEBHOOK_PARSE] ID numérico inválido no externalReference: '{}'", externalReference);
            return null;
        }
    }

    /** Resolve o type com prioridade: query param → body (type) → body (topic). */
    private String resolverType(MercadoPagoWebhookPayloadDTO payload, String typeParam) {
        if (typeParam != null && !typeParam.isBlank()) {
            return typeParam;
        }
        if (payload != null) {
            return payload.getNotificationType();
        }
        return null;
    }

    /** Resolve o paymentId com prioridade: query param → body (data.id). */
    private String resolverPaymentId(MercadoPagoWebhookPayloadDTO payload, String dataIdParam) {
        if (dataIdParam != null && !dataIdParam.isBlank()) {
            return dataIdParam;
        }
        if (payload != null) {
            return payload.getPaymentId();
        }
        return null;
    }
}
