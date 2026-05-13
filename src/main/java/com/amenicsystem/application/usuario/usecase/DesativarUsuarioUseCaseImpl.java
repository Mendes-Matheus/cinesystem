package com.amenicsystem.application.usuario.usecase;

import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import com.amenicsystem.domain.usuario.Usuario;
import com.amenicsystem.domain.usuario.UsuarioId;
import com.amenicsystem.domain.usuario.UsuarioRepository;
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
