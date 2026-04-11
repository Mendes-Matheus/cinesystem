package com.cinesystem.application.port.out;

import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.domain.usuario.Usuario;

public interface JwtPort {
    TokenResult gerar(Usuario usuario);
    String extrairEmail(String token);
    boolean isValido(String token);
    void revogar(String token);     // adiciona à blacklist Redis
    boolean isRevogado(String token);
}
