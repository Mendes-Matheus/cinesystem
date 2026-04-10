package com.cinesystem.infrastructure.persistence.filme;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import com.cinesystem.domain.filme.FilmeId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmeQueryAdapter implements FilmeQueryPort {

    private final FilmeJpaRepository jpaRepository;

    public FilmeQueryAdapter(FilmeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<FilmeResult> findAllAtivos(String genero) {
        return jpaRepository.findProjectedAtivos(genero);
    }

    @Override
    public Optional<FilmeResult> findResultById(FilmeId id) {
        return jpaRepository.findProjectedById(id.valor());
    }
}
