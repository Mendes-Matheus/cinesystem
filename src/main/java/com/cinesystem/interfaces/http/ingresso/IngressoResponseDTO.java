package com.cinesystem.interfaces.http.ingresso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoResponseDTO(
    Long id,
    String codigo,
    Long sessaoId,
    Long assentoId,
    String fileira,
    int numeroAssento,
    String tituloFilme,
    LocalDateTime dataHora,
    BigDecimal valorPago,
    String status
) {}
