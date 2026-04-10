package com.cinesystem.interfaces.scheduler;

import com.cinesystem.application.outbox.IngressoCompradoPayload;
import com.cinesystem.application.outbox.OutboxEvent;
import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.application.port.out.EmailPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessorScheduler {

    private final OutboxRepository outboxRepository;
    private final EmailPort emailPort;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5_000)
    @Transactional
    public void processar() {
        List<OutboxEvent> pendentes = outboxRepository.findPendentes(50);
        
        for (OutboxEvent evento : pendentes) {
            try {
                despachar(evento);
                evento.marcarProcessado();
            } catch (Exception ex) {
                log.error("Erro ao processar evento outbox '{}': {}", evento.getId(), ex.getMessage(), ex);
                evento.registrarFalha(ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido");
            } finally {
                outboxRepository.save(evento);
            }
        }
    }

    private void despachar(OutboxEvent evento) throws Exception {
        switch (evento.getEventType()) {
            case "IngressoComprado" -> {
                IngressoCompradoPayload payload = objectMapper.readValue(evento.getPayload(), IngressoCompradoPayload.class);
                emailPort.enviarConfirmacaoIngresso(payload);
            }
            default -> {
                log.warn("Tipo de evento desconhecido: {}", evento.getEventType());
                throw new IllegalArgumentException("Tipo de evento desconhecido");
            }
        }
    }
}
