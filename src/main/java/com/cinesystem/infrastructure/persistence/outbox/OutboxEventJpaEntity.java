package com.cinesystem.infrastructure.persistence.outbox;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;
}
