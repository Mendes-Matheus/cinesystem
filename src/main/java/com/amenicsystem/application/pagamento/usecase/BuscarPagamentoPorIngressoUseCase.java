package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.PagamentoResult;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.usuario.UsuarioId;

public interface BuscarPagamentoPorIngressoUseCase {
    PagamentoResult execute(IngressoId ingressoId, UsuarioId usuarioId);
}
