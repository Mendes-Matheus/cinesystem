package com.cinesystem.interfaces.http.admin;

import java.math.BigDecimal;

public record RelatorioSessaoResponseDTO(
    Long sessaoId, 
    String tituloFilme, 
    String dataHora,
    int totalAssentos, 
    int assentosOcupados, 
    int assentosDisponiveis,
    BigDecimal receitaTotal
) {}
