package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.RelatorioSessaoResult;
import com.amenicsystem.domain.sessao.SessaoId;

public interface RelatorioSessaoUseCase {
    RelatorioSessaoResult execute(SessaoId id);
}
