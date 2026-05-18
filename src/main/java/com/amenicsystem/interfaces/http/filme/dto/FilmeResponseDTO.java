package com.amenicsystem.interfaces.http.filme.dto;

import java.time.LocalDate;

public record FilmeResponseDTO(
    Long id,
    String titulo,
    String genero,
    String classificacao,
    int duracaoMinutos,
    String posterUrl,
    LocalDate dataLancamento
) {}
