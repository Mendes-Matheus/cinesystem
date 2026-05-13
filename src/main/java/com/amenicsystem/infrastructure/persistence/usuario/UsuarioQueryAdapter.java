package com.amenicsystem.infrastructure.persistence.usuario;

import com.amenicsystem.application.port.out.query.UsuarioQueryPort;
import com.amenicsystem.application.usuario.dto.UsuarioResult;
import com.amenicsystem.domain.usuario.UsuarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioQueryAdapter implements UsuarioQueryPort {

    private final UsuarioJpaRepository jpaRepository;

    @Override
    public Page<UsuarioResult> findAll(Pageable pageable) {
        return jpaRepository.findAllProjected(pageable);
    }

    @Override
    public Optional<UsuarioResult> findResultById(UsuarioId id) {
        return jpaRepository.findProjectedById(id.id());
    }
}
