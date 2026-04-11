package com.cinesystem.domain.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;

public record Senha(String hash) {
    
    public static Senha criar(String textoClaro, PasswordEncoder encoder) {
        if (textoClaro == null || textoClaro.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha em claro não pode ser vazia");
        }
        return new Senha(encoder.encode(textoClaro));
    }

    public boolean matches(String textoClaro, PasswordEncoder encoder) {
        return encoder.matches(textoClaro, hash);
    }
}
