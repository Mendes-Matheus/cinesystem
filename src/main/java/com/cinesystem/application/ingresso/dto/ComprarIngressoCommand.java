package com.cinesystem.application.ingresso.dto;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.pagamento.MetodoPagamento;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;

public record ComprarIngressoCommand(
    SessaoId sessaoId,
    AssentoId assentoId,
    UsuarioId usuarioId,
    MetodoPagamento metodoPagamento
) {}
