package com.amenicsystem.application.filme.usecase;

import com.amenicsystem.application.filme.dto.CriarFilmeCommand;
import com.amenicsystem.application.filme.dto.FilmeResult;

public interface CriarFilmeUseCase {
    FilmeResult execute(CriarFilmeCommand command);
}
