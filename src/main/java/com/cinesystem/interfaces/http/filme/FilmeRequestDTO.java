package com.cinesystem.interfaces.http.filme;

import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record FilmeRequestDTO(
    @NotBlank String titulo,
    @NotNull Genero genero,
    @NotNull ClassificacaoEtaria classificacao,
    @Positive int duracaoMinutos,
    String posterUrl,
    @NotNull LocalDate dataLancamento
) {}
