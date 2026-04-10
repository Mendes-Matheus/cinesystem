package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.SessaoResult;
import com.cinesystem.domain.filme.FilmeId;

import java.util.List;

public interface ListarSessoesPorFilmeUseCase {
    List<SessaoResult> execute(FilmeId filmeId);
}
