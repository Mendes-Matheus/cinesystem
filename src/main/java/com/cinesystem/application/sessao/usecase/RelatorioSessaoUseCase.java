package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.RelatorioSessaoResult;
import com.cinesystem.domain.sessao.SessaoId;

public interface RelatorioSessaoUseCase {
    RelatorioSessaoResult execute(SessaoId id);
}
