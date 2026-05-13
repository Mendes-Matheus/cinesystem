package com.amenicsystem.application.port.out.query;

import com.amenicsystem.application.filme.dto.FilmeResult;
import com.amenicsystem.domain.filme.FilmeId;

import java.util.List;
import java.util.Optional;

public interface FilmeQueryPort {
    List<FilmeResult> findAllAtivos(String genero);
    Optional<FilmeResult> findResultById(FilmeId id);
}
