package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.SessaoResult;
import com.amenicsystem.domain.filme.FilmeId;

import java.util.List;

public interface ListarSessoesPorFilmeUseCase {
    List<SessaoResult> execute(FilmeId filmeId);
}
