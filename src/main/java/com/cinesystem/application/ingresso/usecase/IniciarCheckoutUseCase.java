package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.ingresso.dto.IniciarCheckoutCommand;

public interface IniciarCheckoutUseCase {
    IngressoBasicoResult execute(IniciarCheckoutCommand command);
}
