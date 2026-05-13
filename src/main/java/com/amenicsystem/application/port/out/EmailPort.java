package com.amenicsystem.application.port.out;

import com.amenicsystem.application.outbox.IngressoCompradoPayload;

public interface EmailPort {
    void enviarConfirmacaoIngresso(IngressoCompradoPayload payload);
}
