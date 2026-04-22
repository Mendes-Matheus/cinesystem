package com.cinesystem.application.ingresso.dto;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.ingresso.TipoIngresso;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;

public record IniciarCheckoutCommand(
        SessaoId sessaoId,
        AssentoId assentoId,
        UsuarioId usuarioId,
        String guestId,
        TipoIngresso tipo
) {}
