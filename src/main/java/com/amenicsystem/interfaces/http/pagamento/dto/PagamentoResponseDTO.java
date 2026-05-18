package com.amenicsystem.interfaces.http.pagamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
    Long id,
    Long ingressoId,
    String transacaoExternaId,
    BigDecimal valor,
    String metodo,
    String status,
    LocalDateTime criadoEm,
    LocalDateTime processadoEm
) {}
