package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.outbox.IngressoCompradoPayload;
import com.amenicsystem.application.port.out.IngressoEnriquecidoQueryPort;
import com.amenicsystem.application.query.IngressoEnriquecidoDados;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.sessao.SessaoAssento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Assembler responsável por construir o {@link IngressoCompradoPayload} para o Outbox.
 *
 * <h3>Motivação:</h3>
 * <p>Extraído de {@code ConfirmarPagamentoPorWebhookUseCaseImpl} para:</p>
 * <ul>
 *   <li>Reduzir as 8 dependências do use case</li>
 *   <li>Centralizar o enriquecimento de dados (N+1 queries)</li>
 *   <li>Tornar o use case testável sem mocks de repositórios de leitura</li>
 * </ul>
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IngressoCompradoPayloadAssembler {

    private final IngressoEnriquecidoQueryPort ingressoEnriquecidoQueryPort;

    /**
     * Constrói o payload com dados enriquecidos do ingresso.
     * Consultas auxiliares são delegadas para uma Query Port consolidada.
     *
     * @param ingresso      ingresso recém-ativado
     * @param sessaoAssento sessão-assento já carregada (se disponível)
     * @return payload enriquecido para o outbox event
     */
    public IngressoCompradoPayload montar(Ingresso ingresso, SessaoAssento sessaoAssento) {

        Long sessaoAssentoId = sessaoAssento != null ? sessaoAssento.getId() : ingresso.getSessaoAssentoId();

        IngressoEnriquecidoDados dados = ingressoEnriquecidoQueryPort.buscar(
                ingresso.getId(), sessaoAssentoId
        );

        String emailUsuario = dados.emailUsuario().orElseThrow(() -> {
            log.error("[OUTBOX_ASSEMBLER] Falha ao montar payload: Email do usuário ausente para ingressoId={}", ingresso.getId().id());
            return new IllegalStateException("Email do usuário é obrigatório para envio de ingresso");
        });

        return new IngressoCompradoPayload(
                ingresso.getId().id(),
                ingresso.getCodigo().valor(),
                emailUsuario,
                dados.tituloFilme(),
                dados.dataHoraSessao(),
                dados.fileira(),
                dados.numeroAssento(),
                ingresso.getValorPago()
        );
    }
}

