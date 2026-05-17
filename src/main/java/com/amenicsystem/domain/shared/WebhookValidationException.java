package com.amenicsystem.domain.shared;

/**
 * Assinatura HMAC inválida ou header {@code x-signature} ausente/malformado.
 *
 * <p><strong>Non-retryable:</strong> Uma assinatura inválida indica payload potencialmente
 * forjado. Retentar não resolve — o MP nunca envia assinaturas incorretas para
 * requisições legítimas.</p>
 */
public class WebhookValidationException extends WebhookException {

    public WebhookValidationException(String message) {
        super(message, false);
    }
}
