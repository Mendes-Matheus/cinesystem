package com.amenicsystem.infrastructure.persistence.webhook;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de webhook processado — tabela de deduplicação operacional.
 *
 * <p>Lean por design: sem payload/headers para evitar crescimento descontrolado.
 * A constraint UNIQUE em {@code (payment_id, status_processado)} garante idempotência
 * via INSERT ON CONFLICT — dentro da mesma transação do domínio.</p>
 */
@Entity
@Table(
    name = "processed_webhooks",
    uniqueConstraints = @UniqueConstraint(
            name = "uq_processed_webhook",
            columnNames = {"payment_id", "status_processado"}
    )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedWebhookJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 50)
    private String paymentId;

    @Column(name = "status_processado", nullable = false, length = 30)
    private String statusProcessado;

    /** Apenas para tracing/suporte — não é a idempotency key. */
    @Column(name = "notification_id", length = 50)
    private String notificationId;

    @Column(name = "processado_em", nullable = false)
    private LocalDateTime processadoEm;
}
