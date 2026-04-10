package com.cinesystem.application.port.out.query;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.usuario.UsuarioId;

import java.util.List;
import java.util.Optional;

public interface IngressoQueryPort {
    List<IngressoResult> findByUsuario(UsuarioId usuarioId);
    Optional<IngressoResult> findResultById(IngressoId id);
}
