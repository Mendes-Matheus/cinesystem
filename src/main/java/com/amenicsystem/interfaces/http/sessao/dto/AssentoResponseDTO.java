package com.amenicsystem.interfaces.http.sessao.dto;

public record AssentoResponseDTO(
    Long id,
    String fileira,
    int numero,
    String tipo,
    String status
) {}
