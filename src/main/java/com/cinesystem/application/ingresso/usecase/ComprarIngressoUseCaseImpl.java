package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.ComprarIngressoCommand;
import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.outbox.IngressoCompradoPayload;
import com.cinesystem.application.outbox.OutboxEvent;
import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.application.port.out.ReservaAssentoPort;
import com.cinesystem.domain.assento.Assento;
import com.cinesystem.domain.assento.AssentoRepository;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoRepository;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import com.cinesystem.domain.usuario.Usuario;
import com.cinesystem.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComprarIngressoUseCaseImpl implements ComprarIngressoUseCase {

    private final SessaoRepository sessaoRepository;
    private final ReservaAssentoPort reservaPort;
    private final IngressoRepository ingressoRepository;
    private final OutboxRepository outboxRepository;
    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final AssentoRepository assentoRepository;

    public ComprarIngressoUseCaseImpl(SessaoRepository sessaoRepository,
                                      ReservaAssentoPort reservaPort,
                                      IngressoRepository ingressoRepository,
                                      OutboxRepository outboxRepository,
                                      UsuarioRepository usuarioRepository,
                                      FilmeRepository filmeRepository,
                                      AssentoRepository assentoRepository) {
        this.sessaoRepository = sessaoRepository;
        this.reservaPort = reservaPort;
        this.ingressoRepository = ingressoRepository;
        this.outboxRepository = outboxRepository;
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.assentoRepository = assentoRepository;
    }

    @Override
    @Transactional
    public IngressoBasicoResult execute(ComprarIngressoCommand command) {
        SessaoAssento sessaoAssento = sessaoRepository.findSessaoAssento(command.sessaoId(), command.assentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão/Assento não encontrados"));

        Sessao sessao = sessaoRepository.findById(command.sessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não foi encontrada"));

        if (!reservaPort.reservar(command.sessaoId(), command.assentoId(), command.usuarioId().id().toString())) {
            throw new DomainException("O assento já está temporariamente reservado!");
        }

        Usuario usuario = usuarioRepository.findById(command.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Filme filme = filmeRepository.findById(sessao.getFilmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));

        Assento assento = assentoRepository.findById(command.assentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Assento não encontrado"));

        Ingresso ingresso = sessaoAssento.confirmarCompra(command.usuarioId(), sessao);
        sessaoRepository.saveAllAssentos(List.of(sessaoAssento));
        Ingresso salvo = ingressoRepository.save(ingresso);

        IngressoCompradoPayload payload = new IngressoCompradoPayload(
                salvo.getId() != null ? salvo.getId().id() : 0L,
                salvo.getCodigo().valor(),
                usuario.getEmail().valor(),
                filme.getTitulo(),
                sessao.getDataHora(),
                assento.getFileira(),
                assento.getNumero(),
                salvo.getValorPago()
        );

        outboxRepository.save(OutboxEvent.of("IngressoComprado",
                salvo.getId() != null ? String.valueOf(salvo.getId().id()) : null, payload));

        return IngressoBasicoResult.from(salvo);
    }
}
