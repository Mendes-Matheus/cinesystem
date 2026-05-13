package com.amenicsystem.application.ingresso.dto;

import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.ingresso.TipoIngresso;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;

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