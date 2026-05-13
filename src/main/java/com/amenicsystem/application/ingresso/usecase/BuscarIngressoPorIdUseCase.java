package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.usuario.UsuarioId;

public interface BuscarIngressoPorIdUseCase {
    IngressoResult execute(IngressoId ingressoId, UsuarioId usuarioId);
}
