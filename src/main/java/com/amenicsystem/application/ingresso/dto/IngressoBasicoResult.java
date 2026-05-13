package com.amenicsystem.application.ingresso.dto;

import com.amenicsystem.domain.ingresso.Ingresso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoBasicoResult(
    Long id, String codigo, BigDecimal valorPago, String status, LocalDateTime compradoEm
) {
    public static IngressoBasicoResult from(Ingresso ingresso) {
        return new IngressoBasicoResult(
            ingresso.getId() != null ? ingresso.getId().id() : null,
            ingresso.getCodigo().valor(),
            ingresso.getValorPago(),
            ingresso.getStatus().name(),
            ingresso.getCompradoEm()
        );
    }
}
