package com.amenicsystem.application.port.out;

import com.amenicsystem.application.auth.dto.TokenResult;
import com.amenicsystem.domain.usuario.Usuario;

public interface JwtPort {
    TokenResult gerar(Usuario usuario);
    String extrairEmail(String token);
    Long extrairUserId(String token);
    boolean isValido(String token);
    void revogar(String token);     // adiciona à blacklist Redis
    boolean isRevogado(String token);
}
