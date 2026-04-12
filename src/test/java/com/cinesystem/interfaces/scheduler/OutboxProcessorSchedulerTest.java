package com.cinesystem.interfaces.scheduler;

import com.cinesystem.application.outbox.IngressoCompradoPayload;
import com.cinesystem.application.outbox.OutboxEvent;
import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.application.port.out.EmailPort;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorSchedulerTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private EmailPort emailPort;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxProcessorScheduler scheduler;

    @Test
    @DisplayName("Deve processar eventos pendentes e marcar como processado")
    void deveProcessarEventosPendentes_EMarcarComoProcessado() throws Exception {
        // arrange
        var payload = new IngressoCompradoPayload(1L, "XYZ", "teste@teste.com", "Filme", LocalDateTime.now(), "A", 1, new BigDecimal("25.00"));
        var evento = OutboxEvent.of("IngressoComprado", "1", payload);
        
        when(outboxRepository.findPendentes(50)).thenReturn(List.of(evento));
        when(objectMapper.readValue(evento.getPayload(), IngressoCompradoPayload.class)).thenReturn(payload);
        doNothing().when(emailPort).enviarConfirmacaoIngresso(payload);

        // act
        scheduler.processar();

        // assert
        verify(emailPort).enviarConfirmacaoIngresso(payload);
        verify(outboxRepository).save(evento);
        assertThat(evento.getStatus()).isEqualTo("PROCESSADO");
    }

    @Test
    @DisplayName("Deve registrar falha quando EmailPort lança exceção")
    void deveRegistrarFalha_QuandoEmailPortLancaExcecao() throws Exception {
        // arrange
        var payload = new IngressoCompradoPayload(1L, "XYZ", "teste@teste.com", "Filme", LocalDateTime.now(), "A", 1, new BigDecimal("25.00"));
        var evento = OutboxEvent.of("IngressoComprado", "1", payload);
        
        when(outboxRepository.findPendentes(50)).thenReturn(List.of(evento));
        when(objectMapper.readValue(evento.getPayload(), IngressoCompradoPayload.class)).thenReturn(payload);
        doThrow(new RuntimeException("Erro ao enviar email")).when(emailPort).enviarConfirmacaoIngresso(payload);

        // act
        scheduler.processar();

        // assert
        verify(emailPort).enviarConfirmacaoIngresso(payload);
        verify(outboxRepository).save(evento);
        assertThat(evento.getStatus()).isEqualTo("FALHA");
        assertThat(evento.getTentativas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve processar múltiplos eventos em um ciclo")
    void deveProcessarMultiplosEventos_EmUmCiclo() throws Exception {
        // arrange
        var payload = new IngressoCompradoPayload(1L, "XYZ", "teste@teste.com", "Filme", LocalDateTime.now(), "A", 1, new BigDecimal("25.00"));
        var evento1 = OutboxEvent.of("IngressoComprado", "1", payload);
        var evento2 = OutboxEvent.of("IngressoComprado", "2", payload);
        var evento3 = OutboxEvent.of("IngressoComprado", "3", payload);

        when(outboxRepository.findPendentes(50)).thenReturn(List.of(evento1, evento2, evento3));
        when(objectMapper.readValue(anyString(), eq(IngressoCompradoPayload.class))).thenReturn(payload);

        // act
        scheduler.processar();

        // assert
        verify(emailPort, times(3)).enviarConfirmacaoIngresso(any());
        verify(outboxRepository, times(3)).save(any(OutboxEvent.class));
        assertThat(evento1.getStatus()).isEqualTo("PROCESSADO");
        assertThat(evento2.getStatus()).isEqualTo("PROCESSADO");
        assertThat(evento3.getStatus()).isEqualTo("PROCESSADO");
    }
}
