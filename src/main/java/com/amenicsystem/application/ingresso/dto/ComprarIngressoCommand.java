package com.amenicsystem.application.ingresso.dto;

import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.pagamento.MetodoPagamento;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;

public record ComprarIngressoCommand(
        SessaoId sessaoId,
        AssentoId assentoId,
        UsuarioId usuarioId,
        MetodoPagamento metodoPagamento,
        String cpfCliente,
        String nomeCliente
) {}
