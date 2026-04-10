package com.cinesystem.application.port.out;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;

public interface ReservaAssentoPort {
    boolean reservar(SessaoId sessaoId, AssentoId assentoId, UsuarioId usuarioId);
    void liberar(SessaoId sessaoId, AssentoId assentoId);
}
