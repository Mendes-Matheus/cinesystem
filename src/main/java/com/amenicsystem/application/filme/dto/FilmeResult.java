package com.amenicsystem.application.filme.dto;

import com.amenicsystem.domain.filme.Filme;

import java.time.LocalDate;

public record FilmeResult(
        Long id,
        String titulo,
        String genero,
        String classificacao,
        int duracaoMinutos,
        String posterUrl,
        LocalDate dataLancamento
) {
    public static FilmeResult from(Filme filme) {
        return new FilmeResult(
                filme.getId().id(),
                filme.getTitulo(),
                filme.getGenero().name(),
                filme.getClassificacao().codigo(),
                filme.getDuracaoMinutos(),
                filme.getPosterUrl(),
                filme.getDataLancamento()
        );
    }
}
