package com.amenicsystem.domain.filme;

import com.amenicsystem.domain.shared.DomainException;

public record FilmeId(Long id) {
    public FilmeId {
        if (id == null) {
            throw new DomainException("FilmeId não pode ser nulo");
        }
    }
}
