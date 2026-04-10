package com.cinesystem.application.filme.dto;

import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.Genero;

import java.time.LocalDate;

public record CriarFilmeCommand(
        String titulo,
        Genero genero,
        ClassificacaoEtaria classificacao,
        int duracaoMinutos,
        String posterUrl,
        LocalDate dataLancamento
) {}
