package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.domain.assento.*;
import com.cinesystem.domain.filme.*;
import com.cinesystem.domain.ingresso.*;
import com.cinesystem.domain.pagamento.*;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.sessao.*;
import com.cinesystem.domain.usuario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfirmarPagamentoUseCaseTest {

    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private IngressoRepository ingressoRepository;
    @Mock private SessaoRepository sessaoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OutboxRepository outboxRepository;
    @Mock private FilmeRepository filmeRepository;
    @Mock private AssentoRepository assentoRepository;
    @Mock private AutoCloseable mocks;

    @InjectMocks
    private ConfirmarPagamentoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldConfirmPaymentAndActivateTicket() {
        String txId = "tx-123";
        Long ingressoIdL = 10L;
        Long sessaoAssentoIdL = 1L;

        Pagamento pagamento = new Pagamento(
                1L,
                ingressoIdL,
                txId,
                BigDecimal.valueOf(50),
                MetodoPagamento.PIX,
                StatusPagamento.PENDENTE,
                null
        );

        Ingresso ingresso = new Ingresso(
                new IngressoId(ingressoIdL),
                CodigoIngresso.gerar(),
                new UsuarioId(1L),
                sessaoAssentoIdL,
                BigDecimal.valueOf(50),
                StatusIngresso.AGUARDANDO_PAGAMENTO,
                LocalDateTime.now()
        );

        SessaoAssento sessaoAssento = new SessaoAssento(
                sessaoAssentoIdL,
                new SessaoId(1L),
                new AssentoId(1L),
                StatusAssento.RESERVADO,
                LocalDateTime.now().plusMinutes(10),
                null,
                "guest123"
        );

        Usuario usuario = new Usuario(
                new UsuarioId(1L),
                "Test",
                new Email("test@test.com"),
                new Senha("123456"),
                Role.CLIENTE,
                true
        );

        Sessao sessao = new Sessao(
                new SessaoId(1L),
                new FilmeId(1L),
                new SalaId(1L),
                LocalDateTime.now().plusDays(1),
                "Dublado",
                FormatoExibicao._2D,
                BigDecimal.valueOf(50),
                StatusSessao.ATIVA
        );

        Filme filme = new Filme(
                new FilmeId(1L),
                "Filme Teste",
                "Sinopse",
                Genero.ACAO,
                ClassificacaoEtaria.of("12"),
                120,
                "poster.jpg",
                LocalDate.now(),
                true
        );

        Assento assento = new Assento(
                new AssentoId(1L),
                new SalaId(1L),
                "A",
                1,
                TipoAssento.STANDARD
        );

        when(pagamentoRepository.findByTransacaoId(txId)).thenReturn(Optional.of(pagamento));
        when(ingressoRepository.findById(new IngressoId(ingressoIdL))).thenReturn(Optional.of(ingresso));
        when(sessaoRepository.findSessaoAssentoById(sessaoAssentoIdL)).thenReturn(Optional.of(sessaoAssento));

        when(usuarioRepository.findById(any())).thenReturn(Optional.of(usuario));
        when(sessaoRepository.findById(any())).thenReturn(Optional.of(sessao));
        when(filmeRepository.findById(any())).thenReturn(Optional.of(filme));
        when(assentoRepository.findById(any())).thenReturn(Optional.of(assento));

        useCase.execute(txId);

        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
        assertEquals(StatusIngresso.ATIVO, ingresso.getStatus());
        assertEquals(StatusAssento.OCUPADO, sessaoAssento.getStatus());

        verify(ingressoRepository).save(ingresso);
        verify(pagamentoRepository).save(pagamento);
        verify(outboxRepository).save(any());
    }
}