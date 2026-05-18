package com.amenicsystem.interfaces.http.ingresso.dto;

import com.amenicsystem.domain.ingresso.TipoIngresso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para capturar os dados de início de checkout.
 * O guestId não é incluído aqui pois deve ser enviado via Header (X-Guest-ID)
 * para manter a consistência com o endpoint de reserva.
 */
public record CheckoutRequestDTO(
        @NotNull(message = "O ID da sessão é obrigatório")
        Long sessaoId,

        @NotNull(message = "O ID do assento é obrigatório")
        Long assentoId,

        @NotNull(message = "O tipo de ingresso (INTEIRA/MEIA) é obrigatório")
        TipoIngresso tipo,

        @NotBlank(message = "O CPF é obrigatório")
        String cpfCliente,

        @NotBlank(message = "O nome é obrigatório")
        String nomeCliente
) {}