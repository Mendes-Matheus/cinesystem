package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.domain.usuario.UsuarioId;

import java.util.List;

public interface ListarMeusIngressosUseCase {
    List<IngressoResult> execute(UsuarioId usuarioId);
}
