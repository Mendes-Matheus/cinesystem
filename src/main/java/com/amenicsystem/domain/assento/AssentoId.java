package com.amenicsystem.domain.assento;

import com.amenicsystem.domain.shared.DomainException;

public record AssentoId(Long id) {
    public AssentoId {
        if (id == null)
            throw new DomainException("AssentoId não pode ser nulo");
    }
}
