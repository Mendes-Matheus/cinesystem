package com.amenicsystem.application.pagamento.dto;

public record DadosCartaoCommand(
    String token,
    Integer parcelas,
    String emailPagador
) {}
