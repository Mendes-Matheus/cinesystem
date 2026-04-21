package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.FinalizarCompraCommand;
import com.cinesystem.application.ingresso.dto.IngressoResult;

public interface FinalizarCompraUseCase {
    IngressoResult execute(FinalizarCompraCommand command);
}
