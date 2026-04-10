package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.AtualizarFilmeCommand;
import com.cinesystem.application.filme.dto.FilmeResult;

public interface AtualizarFilmeUseCase {
    FilmeResult execute(AtualizarFilmeCommand command);
}
