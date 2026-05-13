package com.amenicsystem.infrastructure.persistence.ingresso;

import com.amenicsystem.domain.ingresso.CodigoIngresso;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IngressoRepositoryAdapter implements IngressoRepository {

    private final IngressoJpaRepository jpaRepository;
    private final IngressoJpaMapper mapper;

    public IngressoRepositoryAdapter(IngressoJpaRepository jpaRepository, IngressoJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Ingresso save(Ingresso ingresso) {
        IngressoJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(ingresso));
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Ingresso> findById(IngressoId id) {
        return jpaRepository.findById(id.id()).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Ingresso> findByCodigo(CodigoIngresso codigo) {
        return jpaRepository.findByCodigo(codigo.valor()).map(mapper::toDomainEntity);
    }
}
