package com.cinesystem.infrastructure.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    @Query("""
        SELECT o FROM OutboxEventJpaEntity o
        WHERE o.status = 'PENDENTE'
        ORDER BY o.criadoEm ASC
        LIMIT :limit
        """)
    List<OutboxEventJpaEntity> findPendentes(@Param("limit") int limit);
}
