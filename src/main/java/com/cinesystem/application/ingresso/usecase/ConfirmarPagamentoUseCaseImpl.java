// src/main/java/com/cinesystem/application/ingresso/usecase/ConfirmarPagamentoUseCaseImpl.java
package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.outbox.IngressoCompradoPayload;
import com.cinesystem.application.outbox.OutboxEvent;
import com.cinesystem.application.outbox.OutboxRepository;
import com.cinesystem.domain.assento.Assento;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.assento.AssentoRepository;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.domain.ingresso.*;
import com.cinesystem.domain.pagamento.*;
import com.cinesystem.domain.sessao.*;
import com.cinesystem.domain.usuario.*;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfirmarPagamentoUseCaseImpl implements ConfirmarPagamentoUseCase {

    private final PagamentoRepository pagamentoRepository;
    private final IngressoRepository ingressoRepository;
    private final SessaoRepository sessaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OutboxRepository outboxRepository;

    // Novas dependências necessárias para montar o payload do e-mail
    private final FilmeRepository filmeRepository;
    private final AssentoRepository assentoRepository;

    @Override
    @Transactional
    public void execute(String transacaoId) {
        Pagamento pagamento = pagamentoRepository.findByTransacaoId(transacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não localizado"));

        Ingresso ingresso = ingressoRepository.findById(new IngressoId(pagamento.getIngressoId()))
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso não encontrado"));
        ingresso.ativar();

        SessaoAssento assento = sessaoRepository.findSessaoAssentoById(ingresso.getSessaoAssentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Assento não encontrado"));
        assento.efetivarOcupacao();

        ingressoRepository.save(ingresso);
        pagamento.aprovar(transacaoId);
        pagamentoRepository.save(pagamento);
        sessaoRepository.saveAllAssentos(List.of(assento));

        // Dispara o evento completo para o Outbox
        registrarEventoOutbox(ingresso, assento);
    }

    /**
     * Implementação completa do registro no Outbox
     */
    private void registrarEventoOutbox(Ingresso ingresso, SessaoAssento sessaoAssento) {

        // 1. Busca os dados do usuário (e-mail)
        Usuario usuario = usuarioRepository.findById(ingresso.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // 2. Busca os dados da Sessão (Data e Hora)
        Sessao sessao = sessaoRepository.findById(sessaoAssento.getSessaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));

        // 3. Busca os dados do Filme (Título)
        Filme filme = filmeRepository.findById(sessao.getFilmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));

        // 4. Busca os dados do Assento Físico (Fileira e Número)
        Assento assento = assentoRepository.findById(sessaoAssento.getAssentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Assento não encontrado"));

        // 5. Monta o Payload completo esperado pelo agendador de e-mails
        IngressoCompradoPayload payload = new IngressoCompradoPayload(
                ingresso.getId().id(),
                ingresso.getCodigo().valor(),
                usuario.getEmail().valor(),
                filme.getTitulo(),
                sessao.getDataHora(),
                assento.getFileira(),
                assento.getNumero(),
                ingresso.getValorPago()
        );

        // 6. Salva o evento na tabela outbox_events para ser processado assincronamente
        outboxRepository.save(OutboxEvent.of(
                "IngressoComprado",
                String.valueOf(ingresso.getId().id()),
                payload
        ));
    }
}