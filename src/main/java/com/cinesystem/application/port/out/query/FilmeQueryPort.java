package com.cinesystem.application.port.out.query;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.domain.filme.FilmeId;

import java.util.List;
import java.util.Optional;

public interface FilmeQueryPort {
    List<FilmeResult> findAllAtivos(String genero);
    Optional<FilmeResult> findResultById(FilmeId id);
}
