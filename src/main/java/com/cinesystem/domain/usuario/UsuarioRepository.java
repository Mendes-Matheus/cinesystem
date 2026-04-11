package com.cinesystem.domain.usuario;

import java.util.Optional;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(UsuarioId id);
    Optional<Usuario> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
