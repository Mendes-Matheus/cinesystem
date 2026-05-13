package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.domain.sessao.SessaoId;

public interface CancelarSessaoUseCase {
    void execute(SessaoId sessaoId);
}
