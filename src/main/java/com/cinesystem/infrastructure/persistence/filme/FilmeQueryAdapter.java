package com.cinesystem.infrastructure.persistence.filme;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import com.cinesystem.domain.filme.FilmeId;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class FilmeQueryAdapter implements FilmeQueryPort {

    private final FilmeJpaRepository jpaRepository;
    private final FilmeJpaMapper mapper;

    public FilmeQueryAdapter(FilmeJpaRepository jpaRepository, FilmeJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<FilmeResult> findAllAtivos(String genero) {
        return jpaRepository.findByAtivoTrue().stream()
                .filter(filme -> genero == null || filme.getGenero().name().equalsIgnoreCase(genero))
                .sorted(Comparator.comparing(FilmeJpaEntity::getDataLancamento).reversed())
                .map(mapper::toDomainEntity)
                .map(FilmeResult::from)
                .toList();
    }

    @Override
    public Optional<FilmeResult> findResultById(FilmeId filmeId) {
        return jpaRepository.findById(filmeId.id())
                .map(mapper::toDomainEntity)
                .map(FilmeResult::from);
    }
}
