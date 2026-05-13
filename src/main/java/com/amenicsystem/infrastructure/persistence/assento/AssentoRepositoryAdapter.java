package com.amenicsystem.infrastructure.persistence.assento;

import com.amenicsystem.domain.assento.Assento;
import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.assento.AssentoRepository;
import com.amenicsystem.domain.sala.SalaId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AssentoRepositoryAdapter implements AssentoRepository {

    private final AssentoJpaRepository jpaRepository;
    private final AssentoJpaMapper mapper;

    public AssentoRepositoryAdapter(AssentoJpaRepository jpaRepository, AssentoJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Assento> findBySala(SalaId salaId) {
        return jpaRepository.findBySalaId(salaId.id()).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Assento> findById(AssentoId id) {
        return jpaRepository.findById(id.id())
                .map(mapper::toDomainEntity);
    }

    @Override
    public Assento save(Assento assento) {
        AssentoJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(assento));
        return mapper.toDomainEntity(saved);
    }
}
