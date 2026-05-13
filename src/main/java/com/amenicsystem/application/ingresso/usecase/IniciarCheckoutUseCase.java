package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoBasicoResult;
import com.amenicsystem.application.ingresso.dto.IniciarCheckoutCommand;

public interface IniciarCheckoutUseCase {
    IngressoBasicoResult execute(IniciarCheckoutCommand command);
}
