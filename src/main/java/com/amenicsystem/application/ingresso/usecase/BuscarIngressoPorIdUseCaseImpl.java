package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.application.port.out.query.IngressoQueryPort;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import com.amenicsystem.domain.usuario.UsuarioId;
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
