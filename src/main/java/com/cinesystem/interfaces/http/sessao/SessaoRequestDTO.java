package com.cinesystem.interfaces.http.sessao;

import com.cinesystem.domain.sessao.FormatoExibicao;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessaoRequestDTO(
    @NotNull Long filmeId,
    @NotNull Long salaId,
    @NotNull @Future LocalDateTime dataHora,
    @NotBlank String idioma,
    @NotNull FormatoExibicao formato,
    @NotNull @Positive BigDecimal preco
) {}
