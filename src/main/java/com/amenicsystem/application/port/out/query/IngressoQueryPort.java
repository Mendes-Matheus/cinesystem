package com.amenicsystem.application.port.out.query;

import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.usuario.UsuarioId;

import java.util.List;
import java.util.Optional;

public interface IngressoQueryPort {
    List<IngressoResult> findByUsuario(UsuarioId usuarioId);
    Optional<IngressoResult> findResultById(IngressoId id);
    List<IngressoResult> findBySessaoId(com.amenicsystem.domain.sessao.SessaoId sessaoId);
}
