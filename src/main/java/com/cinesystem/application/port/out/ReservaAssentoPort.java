package com.cinesystem.application.port.out;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;

public interface ReservaAssentoPort {
    boolean reservar(SessaoId sessaoId, AssentoId assentoId, String identificador);
    void liberar(SessaoId sessaoId, AssentoId assentoId);
}