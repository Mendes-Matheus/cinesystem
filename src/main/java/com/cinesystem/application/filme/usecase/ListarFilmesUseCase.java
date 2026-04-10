package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;

import java.util.List;

public interface ListarFilmesUseCase {
    List<FilmeResult> execute(String genero);
}
