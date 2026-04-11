package com.cinesystem.infrastructure.email;

import com.cinesystem.application.outbox.IngressoCompradoPayload;
import com.cinesystem.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JavaMailEmailAdapter implements EmailPort {

    @Override
    public void enviarConfirmacaoIngresso(IngressoCompradoPayload payload) {
        log.info("Simulando envio de e-mail para: {} - Ingresso: {}", payload.emailUsuario(), payload.codigo());
        // TODO: fazer a implementação real usando JavaMailSender
    }
}
