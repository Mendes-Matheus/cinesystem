package com.cinesystem.application.ingresso.dto;

import com.cinesystem.domain.ingresso.Ingresso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoBasicoResult(
    Long id, String codigo, BigDecimal valorPago, String status, LocalDateTime compradoEm, String qrCodePix, String qrCodePixBase64
) {
    public static IngressoBasicoResult from(Ingresso ingresso, String qrCodePix, String qrCodePixBase64) {
        return new IngressoBasicoResult(
            ingresso.getId() != null ? ingresso.getId().id() : null,
            ingresso.getCodigo().valor(),
            ingresso.getValorPago(),
            ingresso.getStatus().name(),
            ingresso.getCompradoEm(),
            qrCodePix,
            qrCodePixBase64
        );
    }
    
    public static IngressoBasicoResult from(Ingresso ingresso) {
        return from(ingresso, null, null);
    }
}
