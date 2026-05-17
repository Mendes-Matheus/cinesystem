package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.outbox.IngressoCompradoPayload;
import com.amenicsystem.domain.assento.Assento;
import com.amenicsystem.domain.assento.AssentoRepository;
import com.amenicsystem.domain.filme.Filme;
import com.amenicsystem.domain.filme.FilmeRepository;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.sessao.Sessao;
import com.amenicsystem.domain.sessao.SessaoAssento;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.usuario.UsuarioRepository;
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
 * <h3>Observabilidade:</h3>
 * <p>Loga WARN para cada campo que ficou nulo — evita falha silenciosa
 * quando o consumidor do outbox precisar desses dados.</p>
 *
 * <h3>Posição Arquitetural:</h3>
 * <p>Reside na camada de <em>application</em> porque coordena repositórios
 * de domínio para montar um DTO de aplicação. Não contém regra de negócio.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IngressoCompradoPayloadAssembler {

    private final UsuarioRepository usuarioRepository;
    private final AssentoRepository assentoRepository;
    private final SessaoRepository sessaoRepository;
    private final FilmeRepository filmeRepository;

    /**
     * Constrói o payload com dados enriquecidos do ingresso.
     * Consultas auxiliares são feitas aqui (não no use case).
     *
     * @param ingresso      ingresso recém-ativado
     * @param sessaoAssento sessão-assento já carregada (reutilização — evita query duplicada)
     * @return payload enriquecido para o outbox event
     */
    public IngressoCompradoPayload montar(Ingresso ingresso, SessaoAssento sessaoAssento) {

        // ── Email do usuário ──
        String emailUsuario = null;
        if (ingresso.getUsuarioId() != null) {
            emailUsuario = usuarioRepository.findById(ingresso.getUsuarioId())
                    .map(u -> u.getEmail().valor())
                    .orElse(null);
        }
        if (emailUsuario == null) {
            log.warn("[OUTBOX_ASSEMBLER] Email do usuário não encontrado — ingressoId={}, usuarioId={}. " +
                            "Email de confirmação não poderá ser enviado.",
                    ingresso.getId().id(), ingresso.getUsuarioId());
        }

        // ── Dados do assento ──
        String fileira = null;
        int numeroAssento = 0;

        if (sessaoAssento != null) {
            Assento assento = assentoRepository.findById(sessaoAssento.getAssentoId()).orElse(null);
            if (assento != null) {
                fileira = assento.getFileira();
                numeroAssento = assento.getNumero();
            } else {
                log.warn("[OUTBOX_ASSEMBLER] Assento não encontrado — sessaoAssentoId={}, ingressoId={}. " +
                                "Localização do assento ficará ausente no payload.",
                        sessaoAssento.getAssentoId(), ingresso.getId().id());
            }
        } else {
            log.warn("[OUTBOX_ASSEMBLER] SessaoAssento não encontrada — sessaoAssentoId={}, ingressoId={}. " +
                            "Dados de assento e sessão ficarão ausentes no payload.",
                    ingresso.getSessaoAssentoId(), ingresso.getId().id());
        }

        // ── Dados da sessão e filme ──
        String tituloFilme = null;
        java.time.LocalDateTime dataHora = null;

        if (sessaoAssento != null) {
            Sessao sessao = sessaoRepository.findById(sessaoAssento.getSessaoId()).orElse(null);
            if (sessao != null) {
                dataHora = sessao.getDataHora();
                Filme filme = filmeRepository.findById(sessao.getFilmeId()).orElse(null);
                if (filme != null) {
                    tituloFilme = filme.getTitulo();
                } else {
                    log.warn("[OUTBOX_ASSEMBLER] Filme não encontrado — filmeId={}, ingressoId={}. " +
                                    "Título do filme ficará ausente no payload.",
                            sessao.getFilmeId(), ingresso.getId().id());
                }
            } else {
                log.warn("[OUTBOX_ASSEMBLER] Sessão não encontrada — sessaoId={}, ingressoId={}. " +
                                "Data/hora e título do filme ficarão ausentes no payload.",
                        sessaoAssento.getSessaoId(), ingresso.getId().id());
            }
        }

        return new IngressoCompradoPayload(
                ingresso.getId().id(),
                ingresso.getCodigo().valor(),
                emailUsuario,
                tituloFilme,
                dataHora,
                fileira,
                numeroAssento,
                ingresso.getValorPago()
        );
    }
}
