package com.amenicsystem.application.port.out.query;

import com.amenicsystem.application.sessao.dto.AssentoResult;
import com.amenicsystem.application.sessao.dto.SessaoResult;
import com.amenicsystem.domain.filme.FilmeId;
import com.amenicsystem.domain.sessao.SessaoId;

import java.util.List;
import java.util.Optional;

public interface SessaoQueryPort {
    List<SessaoResult> findAtivasByFilme(FilmeId filmeId);
    List<AssentoResult> findAssentosBySessao(SessaoId sessaoId);
    Optional<SessaoResult> findResultById(SessaoId id);
}
