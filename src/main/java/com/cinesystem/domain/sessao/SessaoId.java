package com.cinesystem.domain.sessao;

import com.cinesystem.domain.shared.DomainException;

public record SessaoId(Long id) {
    public SessaoId {
        if (id == null)
            throw new DomainException("SessaoId não pode ser nulo");
    }
}
