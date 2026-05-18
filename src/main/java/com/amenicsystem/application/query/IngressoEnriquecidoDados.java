package com.amenicsystem.application.query;

import java.time.LocalDateTime;
import java.util.Optional;

public record IngressoEnriquecidoDados(
    Optional<String> emailUsuario,
    Optional<String> tituloFilme,
    Optional<LocalDateTime> dataHoraSessao,
    Optional<String> fileira,
    Optional<Integer> numeroAssento
) {}
