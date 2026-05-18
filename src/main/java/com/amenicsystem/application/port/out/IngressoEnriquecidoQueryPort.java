package com.amenicsystem.application.port.out;

import com.amenicsystem.application.query.IngressoEnriquecidoDados;
import com.amenicsystem.domain.ingresso.IngressoId;

public interface IngressoEnriquecidoQueryPort {
    IngressoEnriquecidoDados buscar(IngressoId ingressoId, Long sessaoAssentoId);
}
