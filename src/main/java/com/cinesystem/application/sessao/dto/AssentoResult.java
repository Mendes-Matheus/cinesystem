package com.cinesystem.application.sessao.dto;

import com.cinesystem.domain.assento.Assento;
import com.cinesystem.domain.sessao.SessaoAssento;

public record AssentoResult(
    Long id, String fileira, int numero, String tipo, String status
) {
    public static AssentoResult from(Assento assento, SessaoAssento sessaoAssento) {
        return new AssentoResult(
                sessaoAssento.getAssentoId().id(),
                assento.getFileira(),
                assento.getNumero(),
                assento.getTipo().name(),
                sessaoAssento.getStatus().name()
        );
    }
}
