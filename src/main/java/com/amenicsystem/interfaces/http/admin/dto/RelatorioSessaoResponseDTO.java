package com.amenicsystem.interfaces.http.admin.dto;

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
