package com.amenicsystem.interfaces.http.admin.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
    Long id, 
    String nome, 
    String email,
    String role, 
    boolean ativo, 
    LocalDateTime criadoEm
) {}
