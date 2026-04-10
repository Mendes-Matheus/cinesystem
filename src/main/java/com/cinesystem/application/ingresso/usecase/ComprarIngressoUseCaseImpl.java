package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.ComprarIngressoCommand;
import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.outbox.IngressoCompradoPayload;
import com.cinesystem.application.outbox.OutboxEvent;
import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.application.port.out.ReservaAssentoPort;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoRepository;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComprarIngressoUseCaseImpl implements ComprarIngressoUseCase {

    private final SessaoRepository sessaoRepository;
    private final ReservaAssentoPort reservaPort;
    private final IngressoRepository ingressoRepository;
    private final OutboxRepository outboxRepository;

    public ComprarIngressoUseCaseImpl(SessaoRepository sessaoRepository,
                                      ReservaAssentoPort reservaPort,
                                      IngressoRepository ingressoRepository,
                                      OutboxRepository outboxRepository) {
        this.sessaoRepository = sessaoRepository;
        this.reservaPort = reservaPort;
        this.ingressoRepository = ingressoRepository;
        this.outboxRepository = outboxRepository;
    }

    @Override
    @Transactional
    public IngressoBasicoResult execute(ComprarIngressoCommand command) {
        SessaoAssento sessaoAssento = sessaoRepository.findSessaoAssento(command.sessaoId(), command.assentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão/Assento não encontrados"));

        Sessao sessao = sessaoRepository.findById(command.sessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não foi encontrada"));

        if (!reservaPort.reservar(command.sessaoId(), command.assentoId(), command.usuarioId())) {
            throw new DomainException("O assento já está temporariamente reservado!");
        }

        Ingresso ingresso = sessaoAssento.confirmarCompra(command.usuarioId(), sessao);
        Ingresso salvo = ingressoRepository.save(ingresso);

        IngressoCompradoPayload payload = new IngressoCompradoPayload(
                salvo.getId() != null ? salvo.getId().id() : 0L,
                salvo.getCodigo().valor(),
                "usuario@" + command.usuarioId().valor() + ".com", 
                "Filme Placeholder", 
                sessao.getDataHora(),
                "A", 
                1,   
                salvo.getValorPago()
        );

        outboxRepository.save(OutboxEvent.of("IngressoComprado", 
                salvo.getId() != null ? String.valueOf(salvo.getId().id()) : null, payload));

        return IngressoBasicoResult.from(salvo);
    }
}
