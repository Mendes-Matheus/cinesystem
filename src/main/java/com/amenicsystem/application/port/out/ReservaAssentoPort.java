package com.amenicsystem.application.port.out;

import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.sessao.SessaoId;

public interface ReservaAssentoPort {
    boolean reservar(SessaoId sessaoId, AssentoId assentoId, String identificador);
    void liberar(SessaoId sessaoId, AssentoId assentoId);
}