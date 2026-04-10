package com.cinesystem.domain.ingresso;

import java.util.UUID;

public record CodigoIngresso(String valor) {
    public static CodigoIngresso gerar() {
        return new CodigoIngresso(UUID.randomUUID().toString());
    }
}
