package com.amenicsystem.application.filme.usecase;

import com.amenicsystem.application.filme.dto.FilmeResult;
import com.amenicsystem.domain.filme.FilmeId;

public interface BuscarFilmePorIdUseCase {
    FilmeResult execute(FilmeId id);
}
