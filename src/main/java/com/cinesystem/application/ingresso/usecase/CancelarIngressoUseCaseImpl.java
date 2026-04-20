package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.CancelarIngressoCommand;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.pagamento.PagamentoRepository;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelarIngressoUseCaseImpl implements CancelarIngressoUseCase {

    private final IngressoRepository ingressoRepository;
    private final PagamentoRepository pagamentoRepository;

    public CancelarIngressoUseCaseImpl(IngressoRepository ingressoRepository, PagamentoRepository pagamentoRepository) {
        this.ingressoRepository = ingressoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    @Transactional
    public void execute(CancelarIngressoCommand command) {
        Ingresso ingresso = ingressoRepository.findById(command.ingressoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado"));

        if (!ingresso.getUsuarioId().equals(command.usuarioId())) {
            throw new AccessDeniedException("Acesso negado");
        }

        // 1. Cancela o ingresso
        ingresso.cancelar();
        ingressoRepository.save(ingresso);

        // 2. Busca o pagamento vinculado e realiza o estorno
        pagamentoRepository.findByIngressoId(ingresso.getId().id())
                .ifPresent(pagamento -> {
                    pagamento.estornar();
                    pagamentoRepository.save(pagamento);

                    // TODO: Chamar um PagamentoGatewayPort para notificar a operadora do cartão
                    // (MercadoPago, Stripe, etc.) sobre o estorno de forma síncrona
                    // ou enfileirar no Outbox.
                });
    }
}