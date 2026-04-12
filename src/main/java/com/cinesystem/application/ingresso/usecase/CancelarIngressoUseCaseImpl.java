package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.CancelarIngressoCommand;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelarIngressoUseCaseImpl implements CancelarIngressoUseCase {

    private final IngressoRepository ingressoRepository;

    public CancelarIngressoUseCaseImpl(IngressoRepository ingressoRepository) {
        this.ingressoRepository = ingressoRepository;
    }

    @Override
    @Transactional
    public void execute(CancelarIngressoCommand command) {
        Ingresso ingresso = ingressoRepository.findById(command.ingressoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado"));

        if (!ingresso.getUsuarioId().equals(command.usuarioId())) {
            throw new AccessDeniedException("Acesso negado");
        }

        ingresso.cancelar();
        ingressoRepository.save(ingresso);
    }
}
