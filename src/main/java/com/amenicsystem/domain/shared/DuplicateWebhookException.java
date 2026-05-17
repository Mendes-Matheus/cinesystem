package com.amenicsystem.domain.shared;

/**
 * Webhook já foi processado anteriormente para este {@code (paymentId, status)}.
 *
 * <p><strong>Non-retryable:</strong> A combinação paymentId+status já existe em
 * {@code processed_webhooks}. O evento foi processado com sucesso anteriormente.
 * Retentar resultaria em duplicate — a constraint de banco garantirá o mesmo resultado.</p>
 *
 * <p>Esta exceção é lançada dentro da transação do domínio quando o
 * {@code INSERT ON CONFLICT DO NOTHING} retorna 0 rows affected.</p>
 */
public class DuplicateWebhookException extends WebhookException {

    public DuplicateWebhookException(String paymentId, String status) {
        super(String.format("Webhook duplicado — paymentId=%s, status=%s já processado",
                paymentId, status), false);
    }
}
