package com.cinesystem.application.sessao.dto;

import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.sessao.FormatoExibicao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CriarSessaoCommand(
    FilmeId filmeId, SalaId salaId, LocalDateTime dataHora,
    String idioma, FormatoExibicao formato, BigDecimal preco
) {}
