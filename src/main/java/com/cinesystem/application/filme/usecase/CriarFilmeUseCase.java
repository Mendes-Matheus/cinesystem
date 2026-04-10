package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.CriarFilmeCommand;
import com.cinesystem.application.filme.dto.FilmeResult;

public interface CriarFilmeUseCase {
    FilmeResult execute(CriarFilmeCommand command);
}
