package com.cinesystem.interfaces.http.admin;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
    Long id, 
    String nome, 
    String email,
    String role, 
    boolean ativo, 
    LocalDateTime criadoEm
) {}
