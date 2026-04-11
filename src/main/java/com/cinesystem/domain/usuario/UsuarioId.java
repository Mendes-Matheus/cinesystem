package com.cinesystem.domain.usuario;

import com.cinesystem.domain.shared.DomainException;

public record UsuarioId(Long id) {
    public UsuarioId {
        if (id == null) {
            throw new DomainException("ID do usuário não pode ser nulo");
        }
    }

    // fallback mapping used previously for compatibility
    public Long valor() {
        return id;
    }
}
