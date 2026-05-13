package com.amenicsystem.application.auth.usecase;

import com.amenicsystem.application.auth.dto.CadastroCommand;
import com.amenicsystem.application.auth.dto.TokenResult;

public interface CadastrarUsuarioUseCase {
    TokenResult execute(CadastroCommand command);
}
