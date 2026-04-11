package com.cinesystem.application.sessao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RelatorioSessaoResult(
    Long sessaoId, 
    String tituloFilme, 
    LocalDateTime dataHora,
    int totalAssentos, 
    int assentosOcupados, 
    int assentosDisponiveis,
    BigDecimal receitaTotal
) {}
