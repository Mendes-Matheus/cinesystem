package com.amenicsystem.infrastructure.persistence.adapter;

import java.time.LocalDateTime;

public record IngressoEnriquecidoRow(
    String emailUsuario,
    String tituloFilme,
    LocalDateTime dataHoraSessao,
    String fileira,
    Integer numeroAssento
) {}
