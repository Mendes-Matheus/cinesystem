package com.cinesystem.application.port.out.query;

import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.application.sessao.dto.SessaoResult;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sessao.SessaoId;

import java.util.List;

public interface SessaoQueryPort {
    List<SessaoResult> findAtivasByFilme(FilmeId filmeId);
    List<AssentoResult> findAssentosBySessao(SessaoId sessaoId);
}
