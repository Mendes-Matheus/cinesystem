package com.amenicsystem.application.filme.usecase;

import com.amenicsystem.application.filme.dto.AtualizarFilmeCommand;
import com.amenicsystem.application.filme.dto.FilmeResult;

public interface AtualizarFilmeUseCase {
    FilmeResult execute(AtualizarFilmeCommand command);
}
