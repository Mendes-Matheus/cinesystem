package com.cinesystem.domain.usuario;

import com.cinesystem.domain.shared.DomainException;
import java.util.regex.Pattern;

public record Email(String valor) {
    private static final Pattern PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");

    public Email {
        if (valor == null || !PATTERN.matcher(valor).matches()) {
            throw new DomainException("E-mail inválido: " + valor);
        }
    }
}
