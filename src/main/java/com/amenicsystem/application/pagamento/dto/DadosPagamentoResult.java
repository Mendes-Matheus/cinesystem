package com.amenicsystem.application.pagamento.dto;

import java.time.LocalDateTime;

public record DadosPagamentoResult(
    String pixQrCode,
    String pixQrCodeTexto,
    String boletoUrl,
    String boletoCodBarras,
    LocalDateTime expiracao
) {}
