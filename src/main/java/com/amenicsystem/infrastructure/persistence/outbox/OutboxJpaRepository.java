package com.amenicsystem.infrastructure.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    @Query("""
        SELECT o FROM OutboxEventJpaEntity o
        WHERE o.status = 'PENDENTE'
           OR (o.status = 'FALHA' AND o.tentativas < 5)
        ORDER BY o.criadoEm ASC
        LIMIT :limit
        """)
    List<OutboxEventJpaEntity> findPendentes(@Param("limit") int limit);
}
