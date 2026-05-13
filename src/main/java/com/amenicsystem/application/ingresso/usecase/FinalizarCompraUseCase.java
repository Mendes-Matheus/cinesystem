package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.FinalizarCompraCommand;
import com.amenicsystem.application.ingresso.dto.IngressoResult;

public interface FinalizarCompraUseCase {
    IngressoResult execute(FinalizarCompraCommand command);
}
