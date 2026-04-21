package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.FinalizarCompraCommand;
import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.application.port.out.ReservaAssentoPort;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoRepository;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoRepository;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinalizarCompraUseCaseImpl implements FinalizarCompraUseCase {

    private final SessaoRepository sessaoRepository;
    private final IngressoRepository ingressoRepository;
    private final ReservaAssentoPort reservaPort;

    @Override
    @Transactional
    public IngressoResult execute(FinalizarCompraCommand command) {
        // 1. Valida a sessão e o assento
        SessaoAssento sessaoAssento = sessaoRepository.findSessaoAssento(command.sessaoId(), command.assentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada"));

        // Precisamos da Sessão para obter o preço e dados para o Result
        Sessao sessao = sessaoRepository.findById(command.sessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));

        // 2. Inicia fluxo de compra e vincula o usuário autenticado
        // O preço base agora vem da entidade Sessao para maior segurança
        Ingresso ingresso = sessaoAssento.iniciarFluxoCompra(command.guestId(), command.tipo(), sessao.getPreco());
        ingresso.vincularUsuario(command.usuarioAutenticadoId());

        // 3. Efetivação e Persistência
        sessaoAssento.efetivarOcupacao(); // Passo 6: Alteração definitiva no banco

        Ingresso salvo = ingressoRepository.save(ingresso);
        sessaoRepository.saveAllAssentos(List.of(sessaoAssento));
        reservaPort.liberar(command.sessaoId(), command.assentoId());

        // 4. Montagem do Resultado Rico
        // Nota: Em uma implementação real, você buscaria o título do filme e detalhes do assento via repositórios
        return IngressoResult.from(salvo, sessao, null, "Título do Filme");
    }

}
