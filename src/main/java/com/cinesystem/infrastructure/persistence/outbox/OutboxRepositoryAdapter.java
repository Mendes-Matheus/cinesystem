package com.cinesystem.infrastructure.persistence.outbox;

import com.cinesystem.application.outbox.OutboxEvent;
import com.cinesystem.application.outbox.OutboxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final OutboxJpaRepository jpaRepository;

    public OutboxRepositoryAdapter(OutboxJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    private OutboxEvent toDomain(OutboxEventJpaEntity entity) {
        if (entity == null) return null;
        return new OutboxEvent(
                entity.getId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getTentativas(),
                entity.getCriadoEm(),
                entity.getProcessadoEm()
        );
    }

    private OutboxEventJpaEntity toEntity(OutboxEvent domain) {
        if (domain == null) return null;
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.setId(domain.getId());
        entity.setEventType(domain.getEventType());
        entity.setAggregateId(domain.getAggregateId());
        entity.setPayload(domain.getPayload());
        entity.setStatus(domain.getStatus());
        entity.setTentativas(domain.getTentativas());
        entity.setCriadoEm(domain.getCriadoEm());
        entity.setProcessadoEm(domain.getProcessadoEm());
        return entity;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventJpaEntity saved = jpaRepository.save(toEntity(event));
        return toDomain(saved);
    }

    @Override
    public List<OutboxEvent> findPendentes(int limit) {
        return jpaRepository.findPendentes(limit).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
