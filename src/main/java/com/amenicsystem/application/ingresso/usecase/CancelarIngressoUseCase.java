package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.CancelarIngressoCommand;

public interface CancelarIngressoUseCase {
    void execute(CancelarIngressoCommand command);
}
