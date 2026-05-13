package com.amenicsystem.application.filme.dto;

import com.amenicsystem.domain.filme.ClassificacaoEtaria;
import com.amenicsystem.domain.filme.FilmeId;
import com.amenicsystem.domain.filme.Genero;

public record AtualizarFilmeCommand(
        FilmeId id,
        String titulo,
        Genero genero,
        ClassificacaoEtaria classificacao,
        int duracaoMinutos,
        String posterUrl
) {}
