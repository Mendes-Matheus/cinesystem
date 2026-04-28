package com.cinesystem.application.port.out;

public record TransacaoGatewayResult(
        String transacaoId,
        String qrCode,
        String qrCodeBase64
) {}
