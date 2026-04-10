package com.cinesystem.application.filme.dto;

import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.filme.Genero;

public record AtualizarFilmeCommand(
        FilmeId id,
        String titulo,
        Genero genero,
        ClassificacaoEtaria classificacao,
        int duracaoMinutos,
        String posterUrl
) {}
