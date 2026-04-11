package com.cinesystem.application.auth.usecase;

import com.cinesystem.application.auth.dto.LoginCommand;
import com.cinesystem.application.auth.dto.TokenResult;

public interface AutenticarUsuarioUseCase {
    TokenResult execute(LoginCommand command);
}
