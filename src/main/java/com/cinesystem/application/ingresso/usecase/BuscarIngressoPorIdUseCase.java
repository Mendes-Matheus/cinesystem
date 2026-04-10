package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.usuario.UsuarioId;

public interface BuscarIngressoPorIdUseCase {
    IngressoResult execute(IngressoId ingressoId, UsuarioId usuarioId);
}
