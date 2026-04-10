package com.cinesystem.interfaces.http.filme;

import java.time.LocalDate;

public record FilmeResponseDTO(
    Long id,
    String titulo,
    String genero,
    String classificacao,
    int duracaoMinutos,
    String posterUrl,
    LocalDate dataLancamento
) {}
