package com.cinesystem.infrastructure.persistence.ingresso;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngressoJpaRepository extends JpaRepository<IngressoJpaEntity, Long> {

    Optional<IngressoJpaEntity> findByCodigo(String codigo);

    @Query("""
        SELECT new com.cinesystem.application.ingresso.dto.IngressoResult(
            i.id, i.codigo, sa.sessao.id, sa.assento.id,
            sa.assento.fileira, sa.assento.numero,
            s.filme.titulo, s.dataHora, i.valorPago, cast(i.status as string)
        )
        FROM IngressoJpaEntity i
        JOIN i.sessaoAssento sa
        JOIN sa.sessao s
        WHERE i.usuarioId = :usuarioId
        ORDER BY i.compradoEm DESC
        """)
    List<IngressoResult> findProjectedByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    @Query("""
        SELECT new com.cinesystem.application.ingresso.dto.IngressoResult(
            i.id, i.codigo, sa.sessao.id, sa.assento.id,
            sa.assento.fileira, sa.assento.numero,
            s.filme.titulo, s.dataHora, i.valorPago, cast(i.status as string)
        )
        FROM IngressoJpaEntity i
        JOIN i.sessaoAssento sa
        JOIN sa.sessao s
        WHERE i.id = :id
        """)
    Optional<IngressoResult> findProjectedById(@Param("id") Long id);
}
