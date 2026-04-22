package com.cinesystem.interfaces.http.ingresso;

import com.cinesystem.domain.ingresso.TipoIngresso;
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
        TipoIngresso tipo
) {}