package com.cinesystem.domain.assento;

import com.cinesystem.domain.shared.DomainException;

public record AssentoId(Long id) {
    public AssentoId {
        if (valor == null)
            throw new DomainException("AssentoId não pode ser nulo");
    }
}
