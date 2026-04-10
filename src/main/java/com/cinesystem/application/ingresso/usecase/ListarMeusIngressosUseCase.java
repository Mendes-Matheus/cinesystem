package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.domain.usuario.UsuarioId;

import java.util.List;

public interface ListarMeusIngressosUseCase {
    List<IngressoResult> execute(UsuarioId usuarioId);
}
