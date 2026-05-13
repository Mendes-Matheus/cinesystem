package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.ReservarAssentoCommand;

public interface ReservarAssentoUseCase {
    void execute(ReservarAssentoCommand command);
}