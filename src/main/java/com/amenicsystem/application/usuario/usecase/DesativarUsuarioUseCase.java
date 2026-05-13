package com.amenicsystem.application.usuario.usecase;

import com.amenicsystem.domain.usuario.UsuarioId;

public interface DesativarUsuarioUseCase {
    void execute(UsuarioId alvoId, UsuarioId adminId);
}
