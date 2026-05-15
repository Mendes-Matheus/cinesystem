package com.amenicsystem.interfaces.http.pagamento;

import com.amenicsystem.application.pagamento.dto.IniciarPagamentoCommand;
import com.amenicsystem.application.pagamento.dto.IniciarPagamentoResult;
import com.amenicsystem.application.pagamento.dto.PagamentoResult;
import org.springframework.stereotype.Component;

@Component
public class PagamentoHttpMapper {

    public IniciarPagamentoCommand toCommand(IniciarPagamentoRequestDTO dto, Long usuarioId) {
        return new IniciarPagamentoCommand(
            dto.sessaoId(), dto.assentoId(), usuarioId, dto.emailPagador(), null);
    }

    public IniciarPagamentoResponseDTO toResponse(IniciarPagamentoResult result) {
        return new IniciarPagamentoResponseDTO(
            result.ingressoId(), 
            result.codigoIngresso(), 
            result.statusIngresso(),
            result.preferenceId(), 
            result.statusPagamento(), 
            result.redirectUrl());
    }

    public PagamentoResponseDTO toResponse(PagamentoResult result) {
        return new PagamentoResponseDTO(
                result.id(),
                result.ingressoId(),
                result.transacaoExternaId(),
                result.valor(),
                result.metodo(),
                result.status(),
                result.criadoEm(),
                result.processadoEm()
        );
    }
}
