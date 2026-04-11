package com.cinesystem.application.auth.usecase;

import com.cinesystem.application.auth.dto.CadastroCommand;
import com.cinesystem.application.auth.dto.TokenResult;

public interface CadastrarUsuarioUseCase {
    TokenResult execute(CadastroCommand command);
}
