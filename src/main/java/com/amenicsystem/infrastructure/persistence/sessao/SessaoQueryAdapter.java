package com.amenicsystem.infrastructure.persistence.sessao;

import com.amenicsystem.application.port.out.query.SessaoQueryPort;
import com.amenicsystem.application.sessao.dto.AssentoResult;
import com.amenicsystem.application.sessao.dto.SessaoResult;
import com.amenicsystem.domain.filme.FilmeId;
import com.amenicsystem.domain.sessao.SessaoId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SessaoQueryAdapter implements SessaoQueryPort {

    private final SessaoJpaRepository sessaoJpaRepository;

    public SessaoQueryAdapter(SessaoJpaRepository sessaoJpaRepository) {
        this.sessaoJpaRepository = sessaoJpaRepository;
    }

    @Override
    public List<SessaoResult> findAtivasByFilme(FilmeId filmeId) {
        return sessaoJpaRepository.findAtivasByFilmeId(filmeId.id());
    }

    @Override
    public List<AssentoResult> findAssentosBySessao(SessaoId sessaoId) {
        return sessaoJpaRepository.findAssentosBySessaoId(sessaoId.id());
    }

    @Override
    public Optional<SessaoResult> findResultById(SessaoId id) {
        return sessaoJpaRepository.findResultById(id.id());
    }
}
