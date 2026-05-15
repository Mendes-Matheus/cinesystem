package com.amenicsystem.application.port.out;

public record ConsultaPagamentoResult(
        String status,
        String externalReference
) {}
