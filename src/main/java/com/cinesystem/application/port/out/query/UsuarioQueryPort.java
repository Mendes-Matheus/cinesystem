package com.cinesystem.application.port.out.query;

import com.cinesystem.application.usuario.dto.UsuarioResult;
import com.cinesystem.domain.usuario.UsuarioId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuarioQueryPort {
    Page<UsuarioResult> findAll(Pageable pageable);
    Optional<UsuarioResult> findResultById(UsuarioId id);
}
