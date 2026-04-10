package com.cinesystem.application.port.out;

import com.cinesystem.application.outbox.IngressoCompradoPayload;

public interface EmailPort {
    void enviarConfirmacaoIngresso(IngressoCompradoPayload payload);
}
