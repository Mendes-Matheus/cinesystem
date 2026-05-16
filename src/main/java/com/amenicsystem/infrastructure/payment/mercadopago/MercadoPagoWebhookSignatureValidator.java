package com.amenicsystem.infrastructure.payment.mercadopago;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Validador de assinatura HMAC-SHA256 para webhooks do Mercado Pago.
 *
 * <h3>Protocolo de Validação (conforme docs oficiais do MP):</h3>
 * <ol>
 *   <li>Extrair {@code ts} (timestamp) e {@code v1} (hash) do header {@code x-signature}</li>
 *   <li>Construir o manifest: {@code id:[data.id];request-id:[x-request-id];ts:[ts];}</li>
 *   <li>Calcular HMAC-SHA256 usando a {@code webhook-secret} como chave</li>
 *   <li>Comparar em constant-time com o {@code v1} do header</li>
 * </ol>
 *
 * <h3>Decisões de Segurança:</h3>
 * <ul>
 *   <li><strong>Constant-time comparison:</strong> Usa {@link MessageDigest#isEqual} para
 *       prevenir timing attacks na comparação de hashes.</li>
 *   <li><strong>Tolerância de timestamp:</strong> Rejeita notificações com mais de 5 minutos
 *       de atraso para mitigar replay attacks. Valor configurável.</li>
 *   <li><strong>Não lança exceção:</strong> Retorna {@code false} para assinaturas inválidas.
 *       O controller sempre retorna HTTP 200 independentemente do resultado.</li>
 * </ul>
 *
 * @see <a href="https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks">
 *      Documentação Oficial - Webhooks</a>
 */
@Component
@Slf4j
public class MercadoPagoWebhookSignatureValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /** Tolerância máxima para o timestamp da notificação (5 minutos em milissegundos). */
    private static final long TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L;

    @Value("${mercado-pago.webhook-secret:}")
    private String webhookSecret;

    /**
     * Valida a autenticidade de uma notificação do Mercado Pago.
     *
     * @param xSignature  valor do header {@code x-signature} (formato: {@code ts=...,v1=...})
     * @param xRequestId  valor do header {@code x-request-id}
     * @param dataId      ID do recurso (query param {@code data.id})
     * @return {@code true} se a assinatura for válida, {@code false} caso contrário
     */
    public boolean validar(String xSignature, String xRequestId, String dataId) {
        // Se o secret não está configurado, pular validação com warning
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("[WEBHOOK_SECURITY] webhook-secret não configurado — validação de assinatura desabilitada. "
                    + "Configure 'mercado-pago.webhook-secret' para ambiente de produção.");
            return true;
        }

        if (xSignature == null || xSignature.isBlank()) {
            log.warn("[WEBHOOK_SECURITY] Header x-signature ausente — notificação potencialmente forjada. "
                    + "dataId={}, requestId={}", dataId, xRequestId);
            return false;
        }

        // 1. Extrair ts e v1 do x-signature
        String ts = null;
        String v1 = null;

        for (String part : xSignature.split(",")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("ts".equals(key)) {
                    ts = value;
                } else if ("v1".equals(key)) {
                    v1 = value;
                }
            }
        }

        if (ts == null || v1 == null) {
            log.warn("[WEBHOOK_SECURITY] Header x-signature com formato inválido — "
                    + "ts ou v1 ausentes. xSignature='{}', dataId={}", xSignature, dataId);
            return false;
        }

        // 2. Validar timestamp (anti replay attack)
        if (!validarTimestamp(ts, dataId)) {
            return false;
        }

        // 3. Construir o manifest conforme documentação do MP
        // Formato: id:[data.id];request-id:[x-request-id];ts:[ts];
        String manifest = String.format("id:%s;request-id:%s;ts:%s;",
                dataId != null ? dataId : "",
                xRequestId != null ? xRequestId : "",
                ts);

        // 4. Calcular HMAC-SHA256
        String computedHash = calcularHmacSha256(manifest);
        if (computedHash == null) {
            log.error("[WEBHOOK_SECURITY] Falha ao calcular HMAC-SHA256 — erro interno. dataId={}", dataId);
            return false;
        }

        // 5. Comparar em constant-time
        boolean valido = MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                v1.getBytes(StandardCharsets.UTF_8));

        if (!valido) {
            log.warn("[WEBHOOK_SECURITY] Assinatura HMAC inválida — notificação rejeitada. "
                    + "dataId={}, requestId={}", dataId, xRequestId);
        } else {
            log.debug("[WEBHOOK_SECURITY] Assinatura HMAC válida. dataId={}, requestId={}", dataId, xRequestId);
        }

        return valido;
    }

    /**
     * Valida se o timestamp da notificação está dentro da tolerância aceitável.
     *
     * @param ts     timestamp em milissegundos (string)
     * @param dataId ID do recurso para logging
     * @return true se o timestamp for aceitável
     */
    private boolean validarTimestamp(String ts, String dataId) {
        try {
            long notificationTimestamp = Long.parseLong(ts);
            long now = System.currentTimeMillis();
            long diff = Math.abs(now - notificationTimestamp);

            if (diff > TIMESTAMP_TOLERANCE_MS) {
                log.warn("[WEBHOOK_SECURITY] Timestamp fora da tolerância — possível replay attack. "
                        + "ts={}, now={}, diffMs={}, toleranceMs={}, dataId={}",
                        ts, now, diff, TIMESTAMP_TOLERANCE_MS, dataId);
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            log.warn("[WEBHOOK_SECURITY] Timestamp com formato inválido: '{}'. dataId={}", ts, dataId);
            return false;
        }
    }

    /**
     * Calcula HMAC-SHA256 do manifest usando o webhook secret como chave.
     *
     * @param manifest string a ser assinada
     * @return hash hexadecimal ou null em caso de erro
     */
    private String calcularHmacSha256(String manifest) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);

            byte[] hashBytes = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[WEBHOOK_SECURITY] Erro ao inicializar HMAC-SHA256: {}", e.getMessage(), e);
            return null;
        }
    }

    /** Converte array de bytes para string hexadecimal lowercase. */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
