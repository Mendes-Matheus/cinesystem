package com.amenicsystem.interfaces.http.pagamento.dto;

import java.time.LocalDateTime;

public record DadosPagamentoResponseDTO(
    String pixQrCode,
    String pixQrCodeTexto,
    String boletoUrl,
    String boletoCodBarras,
    LocalDateTime expiracao
) {}
