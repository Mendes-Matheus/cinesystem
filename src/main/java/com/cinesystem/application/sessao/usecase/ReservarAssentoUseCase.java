package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.ReservarAssentoCommand;

public interface ReservarAssentoUseCase {
    void execute(ReservarAssentoCommand command);
}