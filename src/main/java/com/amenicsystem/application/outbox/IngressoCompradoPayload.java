package com.amenicsystem.application.outbox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public record IngressoCompradoPayload(
    Long ingressoId, String codigo, String emailUsuario,
    Optional<String> tituloFilme, Optional<LocalDateTime> dataHora,
    Optional<String> fileira, Optional<Integer> numeroAssento, BigDecimal valorPago
) {}
