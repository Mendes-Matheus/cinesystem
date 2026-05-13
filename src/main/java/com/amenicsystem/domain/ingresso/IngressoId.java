package com.amenicsystem.domain.ingresso;

import com.amenicsystem.domain.shared.DomainException;

public record IngressoId(Long id) {
    public IngressoId {
        if (id == null) {
            throw new DomainException("O ID do ingresso não pode ser nulo");
        }
    }
}
