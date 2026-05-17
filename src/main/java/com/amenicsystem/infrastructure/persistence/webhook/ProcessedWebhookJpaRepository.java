package com.amenicsystem.infrastructure.persistence.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório JPA para deduplicação de webhooks.
 *
 * <h3>Padrão INSERT FIRST (atômico):</h3>
 * <p>{@link #insertIfAbsent} executa dentro da mesma transação do domínio.
 * Se retornar 0, a combinação {@code (payment_id, status_processado)} já existe
 * → webhook duplicado → lançar {@code DuplicateWebhookException} → rollback seguro.</p>
 */
public interface ProcessedWebhookJpaRepository extends JpaRepository<ProcessedWebhookJpaEntity, Long> {

    /**
     * Insere registro de webhook processado apenas se não existir.
     * Implementa o padrão INSERT FIRST com ON CONFLICT DO NOTHING.
     *
     * @return número de linhas inseridas: 1 = novo, 0 = duplicate
     */
    @Modifying
    @Query(value = """
            INSERT INTO processed_webhooks (payment_id, status_processado, notification_id, processado_em)
            VALUES (:paymentId, :statusProcessado, :notificationId, NOW())
            ON CONFLICT (payment_id, status_processado) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("paymentId") String paymentId,
            @Param("statusProcessado") String statusProcessado,
            @Param("notificationId") String notificationId
    );

}
