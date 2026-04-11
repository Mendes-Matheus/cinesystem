package com.cinesystem.infrastructure.persistence.usuario;

import com.cinesystem.domain.usuario.Email;
import com.cinesystem.domain.usuario.Usuario;
import com.cinesystem.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioJpaMapper mapper;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository, UsuarioJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(usuario));
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Usuario> findById(com.cinesystem.domain.usuario.UsuarioId id) {
        return jpaRepository.findById(id.id()).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Usuario> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.valor()).map(mapper::toDomainEntity);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.valor());
    }
}
