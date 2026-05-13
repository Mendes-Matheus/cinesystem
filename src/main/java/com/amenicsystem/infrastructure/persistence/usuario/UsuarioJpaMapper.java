package com.amenicsystem.infrastructure.persistence.usuario;

import com.amenicsystem.domain.usuario.Email;
import com.amenicsystem.domain.usuario.Senha;
import com.amenicsystem.domain.usuario.Usuario;
import com.amenicsystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Component;

@Component
public class UsuarioJpaMapper {

    public Usuario toDomainEntity(UsuarioJpaEntity entity) {
        if (entity == null) return null;
        return new Usuario(
                new UsuarioId(entity.getId()),
                entity.getNome(),
                new Email(entity.getEmail()),
                new Senha(entity.getSenhaHash()),
                entity.getRole(),
                entity.isAtivo()
        );
    }

    public UsuarioJpaEntity toJpaEntity(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        if (usuario.getId() != null) {
            entity.setId(usuario.getId().id());
        }
        entity.setNome(usuario.getNome());
        entity.setEmail(usuario.getEmail().valor());
        entity.setSenhaHash(usuario.getSenha().hash());
        entity.setRole(usuario.getRole());
        entity.setAtivo(usuario.isAtivo());
        return entity;
    }
}
