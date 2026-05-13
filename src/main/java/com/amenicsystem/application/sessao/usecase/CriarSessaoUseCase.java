package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.CriarSessaoCommand;
import com.amenicsystem.application.sessao.dto.SessaoResult;

public interface CriarSessaoUseCase {
    SessaoResult execute(CriarSessaoCommand command);
}
