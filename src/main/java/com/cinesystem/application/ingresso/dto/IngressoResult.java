package com.cinesystem.application.ingresso.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoResult(
    Long id, String codigo, Long sessaoId, Long assentoId,
    String fileira, int numeroAssento, String tituloFilme,
    LocalDateTime dataHora, BigDecimal valorPago, String status
) {}
