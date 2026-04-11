package com.cinesystem.application.usuario.usecase;

import com.cinesystem.application.port.out.query.UsuarioQueryPort;
import com.cinesystem.application.usuario.dto.UsuarioResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarUsuariosUseCaseImpl implements ListarUsuariosUseCase {

    private final UsuarioQueryPort usuarioQueryPort;

    @Override
    public Page<UsuarioResult> execute(Pageable pageable) {
        return usuarioQueryPort.findAll(pageable);
    }
}
