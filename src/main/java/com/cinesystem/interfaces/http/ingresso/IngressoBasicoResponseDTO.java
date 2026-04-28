package com.cinesystem.interfaces.http.ingresso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoBasicoResponseDTO(
    Long id,
    String codigo,
    BigDecimal valorPago,
    String status,
    LocalDateTime compradoEm,
    String qrCodePix,
    String qrCodePixBase64
) {}
