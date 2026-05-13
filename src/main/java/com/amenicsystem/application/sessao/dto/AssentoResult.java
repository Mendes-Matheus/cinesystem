package com.amenicsystem.application.sessao.dto;

import com.amenicsystem.domain.assento.Assento;
import com.amenicsystem.domain.sessao.SessaoAssento;

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
