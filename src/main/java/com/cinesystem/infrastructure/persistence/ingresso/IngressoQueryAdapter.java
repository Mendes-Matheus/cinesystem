package com.cinesystem.infrastructure.persistence.ingresso;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.application.port.out.query.IngressoQueryPort;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IngressoQueryAdapter implements IngressoQueryPort {

    private final IngressoJpaRepository jpaRepository;

    public IngressoQueryAdapter(IngressoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<IngressoResult> findByUsuario(UsuarioId usuarioId) {
        return jpaRepository.findProjectedByUsuarioId(usuarioId.valor());
    }

    @Override
    public Optional<IngressoResult> findResultById(IngressoId id) {
        return jpaRepository.findProjectedById(id.id());
    }

    @Override
    public List<IngressoResult> findBySessaoId(com.cinesystem.domain.sessao.SessaoId sessaoId) {
        return jpaRepository.findProjectedBySessaoId(sessaoId.id());
    }
}
