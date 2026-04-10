package com.cinesystem.domain.ingresso;

import com.cinesystem.domain.shared.DomainException;

public record IngressoId(Long id) {
    public IngressoId {
        if (id == null) {
            throw new DomainException("O ID do ingresso não pode ser nulo");
        }
    }
}
