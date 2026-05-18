package com.amenicsystem.interfaces.http.auth;

import com.amenicsystem.application.auth.dto.CadastroCommand;
import com.amenicsystem.application.auth.dto.LoginCommand;
import com.amenicsystem.application.auth.dto.TokenResult;
import com.amenicsystem.interfaces.http.auth.dto.AuthRequestDTO;
import com.amenicsystem.interfaces.http.auth.dto.AuthResponseDTO;
import com.amenicsystem.interfaces.http.auth.dto.CadastroRequestDTO;
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
