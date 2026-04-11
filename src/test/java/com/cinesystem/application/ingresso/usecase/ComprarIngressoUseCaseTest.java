package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.ComprarIngressoCommand;
import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.application.port.out.ReservaAssentoPort;
import com.cinesystem.domain.assento.StatusAssento;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.pagamento.MetodoPagamento;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.sessao.FormatoExibicao;
import com.cinesystem.domain.sessao.StatusSessao;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import com.cinesystem.domain.usuario.UsuarioId;
import com.cinesystem.domain.ingresso.StatusIngresso;
import com.cinesystem.domain.ingresso.CodigoIngresso;
import com.cinesystem.domain.sessao.SessaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprarIngressoUseCaseTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private IngressoRepository ingressoRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ReservaAssentoPort reservaPort;

    @InjectMocks
    private ComprarIngressoUseCaseImpl useCase;

    @Test
    @DisplayName("Deve comprar ingresso e registrar no outbox quando assento estiver disponível")
    void deveComprarIngresso_ERegistrarNoOutbox_QuandoAssentoDisponivel() {
        // arrange
        var command = new ComprarIngressoCommand(new SessaoId(1L), new AssentoId(1L), new UsuarioId(1L), MetodoPagamento.CARTAO_CREDITO);
        var sessao = new Sessao(new SessaoId(1L), new FilmeId(1L), new SalaId(1L), LocalDateTime.now().plusDays(1), "LEG", FormatoExibicao._2D, new BigDecimal("25.00"), StatusSessao.ATIVA);
        var sessaoAssento = new SessaoAssento(null, new SessaoId(1L), new AssentoId(1L), StatusAssento.DISPONIVEL, null, null);
        
        var ingresso = new Ingresso(new IngressoId(100L), CodigoIngresso.gerar(), new UsuarioId(1L), 10L, new BigDecimal("25.00"), StatusIngresso.ATIVO, LocalDateTime.now());

        when(sessaoRepository.findSessaoAssento(any(), any())).thenReturn(Optional.of(sessaoAssento));
        when(sessaoRepository.findById(any())).thenReturn(Optional.of(sessao));
        when(reservaPort.reservar(any(), any(), any())).thenReturn(true);
        when(ingressoRepository.save(any())).thenReturn(ingresso);

        // act
        var result = useCase.execute(command);

        // assert
        assertThat(result.status()).isEqualTo("ATIVO");
        verify(ingressoRepository).save(any());
        verify(outboxRepository).save(any()); // verify once
    }

    @Test
    @DisplayName("Deve lançar DomainException quando assento estiver indisponível")
    void deveLancarDomainException_QuandoAssentoIndisponivel() {
        // arrange
        var command = new ComprarIngressoCommand(new SessaoId(1L), new AssentoId(1L), new UsuarioId(1L), MetodoPagamento.CARTAO_CREDITO);
        var sessao = new Sessao(new SessaoId(1L), new FilmeId(1L), new SalaId(1L), LocalDateTime.now().plusDays(1), "LEG", FormatoExibicao._2D, new BigDecimal("25.00"), StatusSessao.ATIVA);
        var sessaoAssento = new SessaoAssento(null, new SessaoId(1L), new AssentoId(1L), StatusAssento.DISPONIVEL, null, null);
        
        when(sessaoRepository.findSessaoAssento(any(), any())).thenReturn(Optional.of(sessaoAssento));
        when(sessaoRepository.findById(any())).thenReturn(Optional.of(sessao));
        when(reservaPort.reservar(any(), any(), any())).thenReturn(false);

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("O assento já está temporariamente reservado");

        verify(ingressoRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando sessão assento não encontrado")
    void deveLancarDomainException_QuandoSessaoAssentoNaoEncontrado() {
        // arrange
        var command = new ComprarIngressoCommand(new SessaoId(1L), new AssentoId(1L), new UsuarioId(1L), MetodoPagamento.CARTAO_CREDITO);
        when(sessaoRepository.findSessaoAssento(any(), any())).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sessão/Assento não encontrados");
                
        verify(reservaPort, never()).reservar(any(), any(), any());
    }
}
