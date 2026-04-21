package com.cinesystem.application.ingresso.dto;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.ingresso.TipoIngresso;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;

import java.math.BigDecimal;

public record FinalizarCompraCommand(
        SessaoId sessaoId,
        AssentoId assentoId,
        String guestId,
        UsuarioId usuarioAutenticadoId,
        TipoIngresso tipo,
        BigDecimal precoBase
) {
}