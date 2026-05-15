package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.PagamentoResult;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import com.amenicsystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BuscarPagamentoPorIngressoUseCaseImpl implements BuscarPagamentoPorIngressoUseCase {

    private final PagamentoRepository pagamentoRepository;
    private final IngressoRepository ingressoRepository;

    public BuscarPagamentoPorIngressoUseCaseImpl(PagamentoRepository pagamentoRepository, IngressoRepository ingressoRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.ingressoRepository = ingressoRepository;
    }

    @Override
    public PagamentoResult execute(IngressoId ingressoId, UsuarioId usuarioId) {
        Ingresso ingresso = ingressoRepository.findById(ingressoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado"));

        if (!usuarioId.equals(ingresso.getUsuarioId())) {
            throw new DomainException("Acesso negado: o ingresso não pertence ao usuário");
        }

        Pagamento pagamento = pagamentoRepository.findByIngressoId(ingressoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para este ingresso"));

        return PagamentoResult.from(pagamento);
    }
}
