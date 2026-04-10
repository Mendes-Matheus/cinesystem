package com.cinesystem.application.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

public class OutboxEvent {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private Long id;
    private String eventType;
    private String aggregateId;
    private String payload;
    private String status;
    private int tentativas;
    private LocalDateTime criadoEm;
    private LocalDateTime processadoEm;

    public OutboxEvent(Long id, String eventType, String aggregateId, String payload, String status, int tentativas, LocalDateTime criadoEm, LocalDateTime processadoEm) {
        this.id = id;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = status;
        this.tentativas = tentativas;
        this.criadoEm = criadoEm;
        this.processadoEm = processadoEm;
    }

    public static OutboxEvent of(String type, String aggregateId, Object payloadObject) {
        try {
            String payloadJson = OBJECT_MAPPER.writeValueAsString(payloadObject);
            return new OutboxEvent(null, type, aggregateId, payloadJson, "PENDENTE", 0, LocalDateTime.now(), null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar payload do OutboxEvent", e);
        }
    }

    public void marcarProcessado() {
        this.status = "PROCESSADO";
        this.processadoEm = LocalDateTime.now();
    }

    public void registrarFalha(String motivo) {
        this.tentativas++;
        this.status = "FALHA";
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public int getTentativas() { return tentativas; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getProcessadoEm() { return processadoEm; }
}
