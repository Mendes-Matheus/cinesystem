package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.application.port.out.query.IngressoQueryPort;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import com.cinesystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Service;

@Service
public class BuscarIngressoPorIdUseCaseImpl implements BuscarIngressoPorIdUseCase {

    private final IngressoQueryPort ingressoQueryPort;
    private final IngressoRepository ingressoRepository;

    public BuscarIngressoPorIdUseCaseImpl(IngressoQueryPort ingressoQueryPort, IngressoRepository ingressoRepository) {
        this.ingressoQueryPort = ingressoQueryPort;
        this.ingressoRepository = ingressoRepository;
    }

    @Override
    public IngressoResult execute(IngressoId ingressoId, UsuarioId usuarioId) {
        Ingresso ingresso = ingressoRepository.findById(ingressoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado"));

        if (!ingresso.getUsuarioId().equals(usuarioId)) {
            throw new DomainException("Acesso negado");
        }

        return ingressoQueryPort.findResultById(ingressoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado"));
    }
}
