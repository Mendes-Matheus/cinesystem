package com.amenicsystem.application.filme.usecase;

import com.amenicsystem.application.filme.dto.FilmeResult;

import java.util.List;

public interface ListarFilmesUseCase {
    List<FilmeResult> execute(String genero);
}
