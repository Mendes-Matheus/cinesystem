package com.amenicsystem.application.pagamento.dto;

import com.amenicsystem.domain.pagamento.Pagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResult(
    Long id,
    Long ingressoId,
    String transacaoExternaId,
    BigDecimal valor,
    String metodo,
    String status,
    LocalDateTime criadoEm,
    LocalDateTime processadoEm
) {
    public static PagamentoResult from(Pagamento p) {
        return new PagamentoResult(
            p.getId().id(),
            p.getIngressoId().id(),
            p.getTransacaoExternaId(),
            p.getValor(),
            p.getMetodo() != null ? p.getMetodo().name() : null,
            p.getStatus().name(),
            p.getCriadoEm(),
            p.getProcessadoEm()
        );
    }
}
