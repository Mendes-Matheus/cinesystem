package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoBasicoResult;
import com.amenicsystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.amenicsystem.application.port.out.ReservaAssentoPort;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IniciarCheckoutUseCaseImpl implements IniciarCheckoutUseCase {

    private final SessaoRepository sessaoRepository;
    private final IngressoRepository ingressoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ReservaAssentoPort reservaPort;

    @Override
    @Transactional
    public IngressoBasicoResult execute(IniciarCheckoutCommand command) {
        // 1. Validar Sessão e Assento
        SessaoAssento sessaoAssento = sessaoRepository.findSessaoAssento(command.sessaoId(), command.assentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada no banco."));

        Sessao sessao = sessaoRepository.findById(command.sessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada."));

        // 2. Regra de Ouro: Validar se a reserva no domínio pertence ao guestId informado
        // Isso impede que um usuário "roube" a reserva de outro ao tentar fazer o checkout
        if (!sessaoAssento.pertenceA(command.guestId())) {
            throw new DomainException("Este assento não está reservado para o seu identificador de visitante.");
        }

        // 3. Criar o Ingresso (Status inicial: AGUARDANDO_PAGAMENTO)
        // O valor é calculado no domínio com base no preço da sessão e no tipo
        Ingresso ingresso = sessaoAssento.iniciarFluxoCompra(command.guestId(), command.tipo(), sessao.getPreco());
        ingresso.vincularUsuario(command.usuarioId());

        // 4. Persistir o ingresso para obter o ID gerado
        Ingresso salvo = ingressoRepository.save(ingresso);

        // 5. Gerar intenção de pagamento vinculada ao ingresso persistido
        Pagamento pagamento = new Pagamento(
                null,
                salvo.getId().id(),
                null,
                salvo.getValorPago(),
                MetodoPagamento.PIX, // Default para checkout inicial
                StatusPagamento.PENDENTE,
                null
        );

        // 6. Persistência
        pagamentoRepository.save(pagamento);

        // Mantemos a reserva no Redis/DB até que o pagamento seja confirmado ou expire
        return IngressoBasicoResult.from(salvo);
    }
}
