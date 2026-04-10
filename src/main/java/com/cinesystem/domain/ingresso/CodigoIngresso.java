package com.cinesystem.domain.ingresso;

import com.cinesystem.domain.shared.DomainException;
import java.util.UUID;

public record CodigoIngresso(String valor) {
    public CodigoIngresso {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DomainException("Código de ingresso inválido");
        }
    }

    public static CodigoIngresso gerar() {
        return new CodigoIngresso(UUID.randomUUID().toString());
    }
}
