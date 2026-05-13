package com.amenicsystem.application.port.out.query;

import com.amenicsystem.application.usuario.dto.UsuarioResult;
import com.amenicsystem.domain.usuario.UsuarioId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuarioQueryPort {
    Page<UsuarioResult> findAll(Pageable pageable);
    Optional<UsuarioResult> findResultById(UsuarioId id);
}
