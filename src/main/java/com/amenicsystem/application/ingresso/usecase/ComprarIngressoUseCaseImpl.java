package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoBasicoResult;
import com.amenicsystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.amenicsystem.application.outbox.OutboxRepository;
import com.amenicsystem.application.port.out.ReservaAssentoPort;
import com.amenicsystem.domain.assento.AssentoRepository;
import com.amenicsystem.domain.filme.FilmeRepository;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import com.amenicsystem.domain.pagamento.MetodoPagamento;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import com.amenicsystem.domain.pagamento.StatusPagamento;
import com.amenicsystem.domain.sessao.Sessao;
import com.amenicsystem.domain.sessao.SessaoAssento;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import com.amenicsystem.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComprarIngressoUseCaseImpl implements ComprarIngressoUseCase {

    private final SessaoRepository sessaoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ReservaAssentoPort reservaPort;
    private final IngressoRepository ingressoRepository;
    private final OutboxRepository outboxRepository;
    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final AssentoRepository assentoRepository;

    public ComprarIngressoUseCaseImpl(SessaoRepository sessaoRepository,
                                      PagamentoRepository pagamentoRepository,
                                      ReservaAssentoPort reservaPort,
                                      IngressoRepository ingressoRepository,
                                      OutboxRepository outboxRepository,
                                      UsuarioRepository usuarioRepository,
                                      FilmeRepository filmeRepository,
                                      AssentoRepository assentoRepository) {
        this.sessaoRepository = sessaoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.reservaPort = reservaPort;
        this.ingressoRepository = ingressoRepository;
        this.outboxRepository = outboxRepository;
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.assentoRepository = assentoRepository;
    }

    @Override // Adicionado para garantir o cumprimento do contrato
    @Transactional
    public IngressoBasicoResult execute(IniciarCheckoutCommand command) {
        // 1. Validações
        SessaoAssento sessaoAssento = sessaoRepository.findSessaoAssento(command.sessaoId(), command.assentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));

        Sessao sessao = sessaoRepository.findById(command.sessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada."));

        if (!sessaoAssento.pertenceA(command.guestId())) {
            throw new DomainException("Este assento não está reservado para o seu identificador de visitante.");
        }

        // 2. Domínio
        Ingresso ingresso = sessaoAssento.iniciarFluxoCompra(command.guestId(), command.tipo(), sessao.getPreco());
        ingresso.vincularUsuario(command.usuarioId());

        // 3. PERSISTÊNCIA NA ORDEM CORRETA (Resolve o erro de FK nula)
        Ingresso salvo = ingressoRepository.save(ingresso);

        // 4. GERA O PAGAMENTO COM O ID DO INGRESSO JÁ GERADO
        Pagamento pagamento = new Pagamento(
                null,
                salvo.getId().id(), // Extrai o Long do record IngressoId
                null,
                salvo.getValorPago(),
                MetodoPagamento.PIX,
                StatusPagamento.PENDENTE,
                null
        );

        pagamentoRepository.save(pagamento);
        sessaoRepository.saveAllAssentos(List.of(sessaoAssento));

        return IngressoBasicoResult.from(salvo);
    }

}
