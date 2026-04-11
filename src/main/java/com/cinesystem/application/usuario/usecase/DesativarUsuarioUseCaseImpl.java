package com.cinesystem.application.usuario.usecase;

import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import com.cinesystem.domain.usuario.Usuario;
import com.cinesystem.domain.usuario.UsuarioId;
import com.cinesystem.domain.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesativarUsuarioUseCaseImpl implements DesativarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void execute(UsuarioId alvoId, UsuarioId adminId) {
        if (alvoId.id().equals(adminId.id())) {
            throw new DomainException("Administrador não pode desativar a própria conta");
        }

        Usuario usuario = usuarioRepository.findById(alvoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + alvoId.id()));

        usuario.desativar();
        usuarioRepository.save(usuario);
    }
}
