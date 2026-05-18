package com.amenicsystem.interfaces.http.filme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record FilmeRequestDTO(
        @NotBlank String titulo,
        @NotBlank String genero,
        @NotBlank String classificacao,
        @Positive int duracaoMinutos,
        String posterUrl,
        @NotNull LocalDate dataLancamento
) {}
