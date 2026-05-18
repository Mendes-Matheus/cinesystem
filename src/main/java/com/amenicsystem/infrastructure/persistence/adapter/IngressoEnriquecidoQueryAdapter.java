package com.amenicsystem.infrastructure.persistence.adapter;

import com.amenicsystem.application.port.out.IngressoEnriquecidoQueryPort;
import com.amenicsystem.application.query.IngressoEnriquecidoDados;
import com.amenicsystem.domain.ingresso.IngressoId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class IngressoEnriquecidoQueryAdapter implements IngressoEnriquecidoQueryPort {

    private final EntityManager entityManager;

    @Override
    public IngressoEnriquecidoDados buscar(IngressoId ingressoId, Long sessaoAssentoId) {
        String jpql = """
            SELECT new com.amenicsystem.infrastructure.persistence.adapter.IngressoEnriquecidoRow(
                u.email, f.titulo, s.dataHora, a.fileira, a.numero
            )
            FROM IngressoJpaEntity i
            LEFT JOIN UsuarioJpaEntity u ON u.id = i.usuarioId
            LEFT JOIN i.sessaoAssento sa
            LEFT JOIN sa.sessao s
            LEFT JOIN s.filme f
            LEFT JOIN sa.assento a
            WHERE i.id = :ingressoId AND sa.id = :sessaoAssentoId
        """;

        try {
            IngressoEnriquecidoRow row = entityManager.createQuery(jpql, IngressoEnriquecidoRow.class)
                    .setParameter("ingressoId", ingressoId.id())
                    .setParameter("sessaoAssentoId", sessaoAssentoId)
                    .getSingleResult();

            if (row.emailUsuario() == null) {
                log.warn("[ADAPTER] Usuário não encontrado para ingressoId={}", ingressoId.id());
            }
            if (row.tituloFilme() == null) {
                log.warn("[ADAPTER] Filme não encontrado para sessaoAssentoId={}, ingressoId={}", sessaoAssentoId, ingressoId.id());
            }
            if (row.dataHoraSessao() == null) {
                log.warn("[ADAPTER] Sessão não encontrada para sessaoAssentoId={}, ingressoId={}", sessaoAssentoId, ingressoId.id());
            }
            if (row.fileira() == null || row.numeroAssento() == null) {
                log.warn("[ADAPTER] Assento não encontrado para sessaoAssentoId={}, ingressoId={}", sessaoAssentoId, ingressoId.id());
            }

            return new IngressoEnriquecidoDados(
                    Optional.ofNullable(row.emailUsuario()),
                    Optional.ofNullable(row.tituloFilme()),
                    Optional.ofNullable(row.dataHoraSessao()),
                    Optional.ofNullable(row.fileira()),
                    Optional.ofNullable(row.numeroAssento())
            );

        } catch (NoResultException e) {
            log.warn("[ADAPTER] Ingresso ou SessaoAssento não encontrados. ingressoId={}, sessaoAssentoId={}", ingressoId.id(), sessaoAssentoId);
            return new IngressoEnriquecidoDados(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
        }
    }
}
