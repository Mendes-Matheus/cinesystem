package com.cinesystem.application.usuario.usecase;

import com.cinesystem.application.usuario.dto.UsuarioResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListarUsuariosUseCase {
    Page<UsuarioResult> execute(Pageable pageable);
}
