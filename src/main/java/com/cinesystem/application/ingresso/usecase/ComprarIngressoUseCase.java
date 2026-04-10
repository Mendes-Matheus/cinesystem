package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.ComprarIngressoCommand;
import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;

public interface ComprarIngressoUseCase {
    IngressoBasicoResult execute(ComprarIngressoCommand command);
}
