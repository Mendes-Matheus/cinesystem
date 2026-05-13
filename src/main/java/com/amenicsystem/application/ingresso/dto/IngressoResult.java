package com.amenicsystem.application.ingresso.dto;

import com.amenicsystem.domain.assento.Assento;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.sessao.Sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngressoResult(
        Long id,
        String codigo,
        Long sessaoId,
        Long assentoId,
        String fileira,
        int numeroAssento,
        String tituloFilme,
        LocalDateTime dataHora,
        BigDecimal valorPago,
        String status
) {
    public static IngressoResult from(Ingresso ingresso, Sessao sessao, Assento assento, String tituloFilme) {
        return new IngressoResult(
                ingresso.getId() != null ? ingresso.getId().id() : null,
                ingresso.getCodigo().valor(),
                sessao.getId().id(),
                assento.getId().id(),
                assento.getFileira(),
                assento.getNumero(),
                tituloFilme,
                sessao.getDataHora(),
                ingresso.getValorPago(),
                ingresso.getStatus().name()
        );
    }
}