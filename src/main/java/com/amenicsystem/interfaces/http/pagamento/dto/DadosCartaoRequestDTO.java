package com.amenicsystem.interfaces.http.pagamento.dto;

import jakarta.validation.constraints.*;

public record DadosCartaoRequestDTO(
    @NotBlank String token,
    @NotNull @Min(1) @Max(12) Integer parcelas,
    @NotBlank @Email String emailPagador
) {}
