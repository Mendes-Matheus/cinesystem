package com.amenicsystem.domain.sessao;

import com.amenicsystem.domain.shared.DomainException;

public record SessaoId(Long id) {
    public SessaoId {
        if (id == null)
            throw new DomainException("SessaoId não pode ser nulo");
    }
}
