package com.cinesystem.interfaces.http.sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessaoResponseDTO(
    Long id,
    Long filmeId,
    String tituloFilme,
    Long salaId,
    String nomeSala,
    LocalDateTime dataHora,
    String idioma,
    String formato,
    BigDecimal preco,
    String status,
    int assentosDisponiveis
) {}
