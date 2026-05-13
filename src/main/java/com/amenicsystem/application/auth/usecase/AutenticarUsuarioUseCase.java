package com.amenicsystem.application.auth.usecase;

import com.amenicsystem.application.auth.dto.LoginCommand;
import com.amenicsystem.application.auth.dto.TokenResult;

public interface AutenticarUsuarioUseCase {
    TokenResult execute(LoginCommand command);
}
