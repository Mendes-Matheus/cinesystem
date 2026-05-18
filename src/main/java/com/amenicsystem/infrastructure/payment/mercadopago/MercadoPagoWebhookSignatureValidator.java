package com.amenicsystem.infrastructure.payment.mercadopago;

import com.amenicsystem.domain.shared.WebhookValidationException;
import jakarta.annotation.PostConstruct;
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
 * <h3>Protocolo (conforme docs oficiais do MP):</h3>
 * <ol>
 *   <li>Extrair {@code ts} (timestamp epoch SEGUNDOS) e {@code v1} (hash hex) de {@code x-signature}</li>
 *   <li>Construir manifest: {@code id:[data.id];request-id:[x-request-id];ts:[ts];}</li>
 *   <li>Calcular HMAC-SHA256 com a {@code webhook-secret} como chave</li>
 *   <li>Comparar em constant-time com {@code v1}</li>
 * </ol>
 *
 * <h3>Guard de Configuração:</h3>
 * <p>{@link #verificarConfiguracao()} valida ao startup se o secret está presente.
 * Em ambiente de produção, a ausência do secret é logada como {@code ERROR} severo.</p>
 *
 * @see <a href="https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks">
 *      Documentação Oficial — Webhooks MP</a>
 */
@Component
@Slf4j
public class MercadoPagoWebhookSignatureValidator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /** Tolerância para o timestamp da notificação (5 minutos em SEGUNDOS). */
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 5 * 60L;

    @Value("${mercado-pago.webhook-secret:}")
    private String webhookSecret;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * Verificação de configuração ao startup.
     * Loga ERROR severo se o secret estiver ausente em produção.
     */
    @PostConstruct
    public void verificarConfiguracao() {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
                log.error("[WEBHOOK_SECURITY_CRITICAL] *** ATENÇÃO DE SEGURANÇA *** " +
                        "mercado-pago.webhook-secret não configurado em ambiente de produção (profile={}). " +
                        "Toda validação de assinatura está DESABILITADA. " +
                        "Configure a variável de ambiente MERCADOPAGO_SECRET_KEY_WEBHOOK imediatamente.",
                        activeProfile);
            } else {
                log.warn("[WEBHOOK_SECURITY] webhook-secret não configurado (profile={}). " +
                        "Validação de assinatura DESABILITADA — aceitável apenas em desenvolvimento.",
                        activeProfile);
            }
        } else {
            log.info("[WEBHOOK_SECURITY] webhook-secret configurado. Validação HMAC-SHA256 ativa.");
        }
    }

    /**
     * Valida a autenticidade de uma notificação do Mercado Pago.
     *
     * @param xSignature  header {@code x-signature} (formato: {@code ts=...,v1=...})
     * @param xRequestId  header {@code x-request-id}
     * @param dataId      query param {@code data.id}
     * @return {@code true} se válida
     * @throws WebhookValidationException se assinatura inválida (non-retryable)
     */
    public boolean validar(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            // Secret não configurado — graceful degradation com warning (já logado no @PostConstruct)
            return true;
        }

        if (xSignature == null || xSignature.isBlank()) {
            throw new WebhookValidationException(
                    String.format("Header x-signature ausente — dataId=%s, requestId=%s", dataId, xRequestId));
        }

        // 1. Extrair ts e v1 do x-signature
        String ts = null;
        String v1 = null;

        for (String part : xSignature.split(",")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if ("ts".equals(key)) ts = value;
                else if ("v1".equals(key)) v1 = value;
            }
        }

        if (ts == null || v1 == null) {
            throw new WebhookValidationException(
                    String.format("Header x-signature com formato inválido (ts ou v1 ausentes): '%s', dataId=%s",
                            xSignature, dataId));
        }

        // 2. Validar timestamp — CORRIGIDO: ambos em SEGUNDOS
        validarTimestamp(ts, dataId);

        // 3. Construir manifest conforme documentação MP
        String manifest = String.format("id:%s;request-id:%s;ts:%s;",
                dataId != null ? dataId : "",
                xRequestId != null ? xRequestId : "",
                ts);

        // 4. Calcular HMAC-SHA256
        String computedHash = calcularHmacSha256(manifest, dataId);

        // 5. Comparar em constant-time (previne timing attacks)
        boolean valido = MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                v1.getBytes(StandardCharsets.UTF_8));

        if (!valido) {
            throw new WebhookValidationException(
                    String.format("Assinatura HMAC inválida — dataId=%s, requestId=%s", dataId, xRequestId));
        }

        log.debug("[WEBHOOK_SECURITY] Assinatura HMAC válida. dataId={}, requestId={}", dataId, xRequestId);
        return true;
    }

    /**
     * Valida o timestamp contra a tolerância de 5 minutos.
     *
     * <h3>Auto-detect de unidade (Bug Fix):</h3>
     * <p>O MP envia {@code ts} em epoch <strong>segundos</strong> (10 dígitos típico).
     * Para robustez, detectamos automaticamente: se o valor tem ≤10 dígitos, tratamos
     * como segundos; se >10, como milissegundos. Ambos convertidos para segundos para
     * comparação com {@code System.currentTimeMillis() / 1000}.</p>
     *
     * @param ts     valor do campo ts do x-signature
     * @param dataId ID do recurso para contexto no log
     * @throws WebhookValidationException se timestamp fora da tolerância ou inválido
     */
    private void validarTimestamp(String ts, String dataId) {
        try {
            long tsValue = Long.parseLong(ts.trim());

            // Auto-detect: epoch seconds (≤10 dígitos) vs millis (>10 dígitos)
            long tsEmSegundos = ts.trim().length() <= 10 ? tsValue : tsValue / 1000L;

            // Comparação em segundos — elimina bug de unidades
            long nowEmSegundos = System.currentTimeMillis() / 1000L;
            long diffSegundos = Math.abs(nowEmSegundos - tsEmSegundos);

            if (diffSegundos > TIMESTAMP_TOLERANCE_SECONDS) {
                throw new WebhookValidationException(
                        String.format("Timestamp fora da tolerância — possível replay attack. " +
                                        "ts=%s (%ds), now=%ds, diff=%ds, tolerance=%ds, dataId=%s",
                                ts, tsEmSegundos, nowEmSegundos, diffSegundos,
                                TIMESTAMP_TOLERANCE_SECONDS, dataId));
            }

            log.debug("[WEBHOOK_SECURITY] Timestamp válido. ts={}s, now={}s, diff={}s",
                    tsEmSegundos, nowEmSegundos, diffSegundos);

        } catch (NumberFormatException e) {
            throw new WebhookValidationException(
                    String.format("Timestamp com formato inválido: '%s', dataId=%s", ts, dataId));
        }
    }

    /**
     * Calcula HMAC-SHA256 do manifest.
     *
     * @throws WebhookValidationException se o algoritmo ou chave forem inválidos —
     *         erro interno que não deve aceitar o webhook silenciosamente
     */
    private String calcularHmacSha256(String manifest, String dataId) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hashBytes = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[WEBHOOK_SECURITY] Falha interna ao calcular HMAC-SHA256: {}", e.getMessage(), e);
            throw new WebhookValidationException(
                    "Falha interna ao calcular HMAC-SHA256 — dataId=" + dataId);
        }
    }

    /** Converte bytes para hexadecimal lowercase. */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
