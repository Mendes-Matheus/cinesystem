package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.cinesystem.application.port.out.PagamentoGatewayPort;
import com.cinesystem.application.port.out.ReservaAssentoPort;
import com.cinesystem.application.port.out.TransacaoGatewayResult;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.assento.StatusAssento;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.ingresso.*;
import com.cinesystem.domain.pagamento.Pagamento;
import com.cinesystem.domain.pagamento.PagamentoRepository;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.sessao.*;
import com.cinesystem.domain.usuario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IniciarCheckoutUseCaseTest {

    @Mock private SessaoRepository sessaoRepository;
    @Mock private IngressoRepository ingressoRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private ReservaAssentoPort reservaPort;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PagamentoGatewayPort pagamentoGatewayPort;

    @InjectMocks
    private IniciarCheckoutUseCaseImpl useCase;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateCheckoutAndReturnQrCode() {
        SessaoId sessaoId = new SessaoId(1L);
        AssentoId assentoId = new AssentoId(1L);
        UsuarioId usuarioId = new UsuarioId(1L);
        String guestId = "guest123";

        SessaoAssento sessaoAssento = new SessaoAssento(
                1L,
                sessaoId,
                assentoId,
                StatusAssento.DISPONIVEL,
                null,
                null,
                null
        );

        sessaoAssento.reservarTemporariamente(guestId, 10);

        Sessao sessao = new Sessao(
                sessaoId,
                new FilmeId(1L),
                new SalaId(1L),
                LocalDateTime.now().plusDays(1),
                "Dublado",
                FormatoExibicao._2D,
                BigDecimal.valueOf(50),
                StatusSessao.ATIVA
        );

        Usuario usuario = new Usuario(
                usuarioId,
                "Test",
                new Email("test@test.com"),
                new Senha("123456"),
                Role.CLIENTE,
                true
        );

        when(sessaoRepository.findSessaoAssento(sessaoId, assentoId))
                .thenReturn(Optional.of(sessaoAssento));

        when(sessaoRepository.findById(sessaoId))
                .thenReturn(Optional.of(sessao));

        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(ingressoRepository.save(any(Ingresso.class)))
                .thenAnswer(i -> {
                    Ingresso ingresso = i.getArgument(0);
                    return new Ingresso(
                            new IngressoId(10L),
                            CodigoIngresso.gerar(),
                            ingresso.getUsuarioId(),
                            ingresso.getSessaoAssentoId(),
                            ingresso.getValorPago(),
                            ingresso.getStatus(),
                            ingresso.getCompradoEm()
                    );
                });

        when(pagamentoGatewayPort.processarPagamentoPix(any(Pagamento.class), anyString()))
                .thenReturn(new TransacaoGatewayResult(
                        "tx-123",
                        "qr-code-pix",
                        "qr-code-base64"
                ));

        IniciarCheckoutCommand command = new IniciarCheckoutCommand(
                sessaoId,
                assentoId,
                usuarioId,
                guestId,
                TipoIngresso.INTEIRA
        );

        IngressoBasicoResult result = useCase.execute(command);


        assertNotNull(result);
        assertEquals("AGUARDANDO_PAGAMENTO", result.status());
        assertEquals("qr-code-pix", result.qrCodePix());
        assertEquals("qr-code-base64", result.qrCodePixBase64());

        verify(pagamentoRepository).save(any(Pagamento.class));
    }
}