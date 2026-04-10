package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.CriarSessaoCommand;
import com.cinesystem.application.sessao.dto.SessaoResult;

public interface CriarSessaoUseCase {
    SessaoResult execute(CriarSessaoCommand command);
}
