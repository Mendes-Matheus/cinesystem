package com.cinesystem.application.usuario.usecase;

import com.cinesystem.domain.usuario.UsuarioId;

public interface DesativarUsuarioUseCase {
    void execute(UsuarioId alvoId, UsuarioId adminId);
}
