package com.cinesystem.domain.filme;

import com.cinesystem.domain.shared.DomainException;

public record FilmeId(Long id) {
    public FilmeId {
        if (valor == null) {
            throw new DomainException("FilmeId não pode ser nulo");
        }
    }
}
