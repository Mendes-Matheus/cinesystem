package com.amenicsystem.application.port.out;

/**
 * Port de saída para persistência de webhooks processados (deduplicação).
 * Implementado pela camada de infraestrutura.
 *
 * <p>O método {@link #tentarRegistrar} deve ser chamado DENTRO da mesma
 * {@code @Transactional} que processa o domínio — garante atomicidade.</p>
 */
public interface ProcessedWebhookRepository {

    /**
     * Tenta registrar o processamento de um webhook.
     * Usa INSERT ON CONFLICT DO NOTHING internamente.
     *
     * @return {@code true} se registrado (novo), {@code false} se já existia (duplicate)
     */
    boolean tentarRegistrar(String paymentId, String statusProcessado, String notificationId);
}
