package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.domain.filme.FilmeId;

public interface BuscarFilmePorIdUseCase {
    FilmeResult execute(FilmeId id);
}
