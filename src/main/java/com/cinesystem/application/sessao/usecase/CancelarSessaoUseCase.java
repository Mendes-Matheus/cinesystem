package com.cinesystem.application.sessao.usecase;

import com.cinesystem.domain.sessao.SessaoId;

public interface CancelarSessaoUseCase {
    void execute(SessaoId sessaoId);
}
