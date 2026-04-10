package com.cinesystem.application.outbox;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoCompradoPayload(
    Long ingressoId, String codigo, String emailUsuario,
    String tituloFilme, LocalDateTime dataHora,
    String fileira, int numeroAssento, BigDecimal valorPago
) {}
