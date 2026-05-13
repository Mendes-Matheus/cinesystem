package com.amenicsystem.application.filme.dto;

import com.amenicsystem.domain.filme.ClassificacaoEtaria;
import com.amenicsystem.domain.filme.Genero;

import java.time.LocalDate;

public record CriarFilmeCommand(
        String titulo,
        Genero genero,
        ClassificacaoEtaria classificacao,
        int duracaoMinutos,
        String posterUrl,
        LocalDate dataLancamento
) {}
