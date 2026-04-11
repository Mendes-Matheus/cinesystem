package com.cinesystem.interfaces.http.auth;

import com.cinesystem.application.auth.dto.CadastroCommand;
import com.cinesystem.application.auth.dto.LoginCommand;
import com.cinesystem.application.auth.dto.TokenResult;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpMapper {

    public CadastroCommand toCommand(CadastroRequestDTO dto) {
        if (dto == null) return null;
        return new CadastroCommand(dto.nome(), dto.email(), dto.senha());
    }

    public LoginCommand toCommand(AuthRequestDTO dto) {
        if (dto == null) return null;
        return new LoginCommand(dto.email(), dto.senha());
    }

    public AuthResponseDTO toResponse(TokenResult result) {
        if (result == null) return null;
        return new AuthResponseDTO(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn()
        );
    }
}
