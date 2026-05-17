package com.amenicsystem.domain.shared;

/**
 * Exceção base para falhas no processamento de webhooks.
 *
 * <h3>Classificação de Retentativa:</h3>
 * <ul>
 *   <li>{@code retryable = false} — erros definitivos. O MP não deve retentar;
 *       retentativas não resolverão o problema (ex: assinatura inválida, duplicate).</li>
 *   <li>{@code retryable = true} — erros transientes. O MP pode retentar com sucesso
 *       (ex: timeout de integração, API indisponível).</li>
 * </ul>
 *
 * <p>O orchestrator usa esta classificação para escolher o nível de log
 * (WARN para non-retryable, ERROR para retryable inesperado).</p>
 */
public abstract class WebhookException extends RuntimeException {

    private final boolean retryable;

    protected WebhookException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    protected WebhookException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    /** @return true se uma nova tentativa do MP tem chance de resolver o problema */
    public boolean isRetryable() {
        return retryable;
    }
}
