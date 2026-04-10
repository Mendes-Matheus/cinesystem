package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.CancelarIngressoCommand;

public interface CancelarIngressoUseCase {
    void execute(CancelarIngressoCommand command);
}
