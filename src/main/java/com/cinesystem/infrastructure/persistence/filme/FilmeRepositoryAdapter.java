package com.cinesystem.infrastructure.persistence.filme;

import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.filme.FilmeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FilmeRepositoryAdapter implements FilmeRepository {

    private final FilmeJpaRepository jpaRepository;
    private final FilmeJpaMapper mapper;

    public FilmeRepositoryAdapter(FilmeJpaRepository jpaRepository, FilmeJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Filme save(Filme filme) {
        FilmeJpaEntity entity = mapper.toJpaEntity(filme);
        FilmeJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Filme> findById(FilmeId id) {
        return jpaRepository.findById(id.valor())
                .map(mapper::toDomainEntity);
    }

    @Override
    public void delete(FilmeId id) {
        jpaRepository.deleteById(id.valor());
    }
}
