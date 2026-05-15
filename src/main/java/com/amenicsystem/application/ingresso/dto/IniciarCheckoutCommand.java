package com.amenicsystem.application.ingresso.dto;

import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.ingresso.TipoIngresso;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;
import jakarta.validation.constraints.NotNull;

public record IniciarCheckoutCommand(
        SessaoId sessaoId,
        AssentoId assentoId,
        UsuarioId usuarioId,
        String guestId,
        TipoIngresso tipo,
        String cpfCliente,
        String nomeCliente
) {}
