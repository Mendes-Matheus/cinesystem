package com.cinesystem.infrastructure.persistence.sessao;

import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.application.sessao.dto.SessaoResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessaoJpaRepository extends JpaRepository<SessaoJpaEntity, Long> {

    @Query("""
        SELECT new com.cinesystem.application.sessao.dto.SessaoResult(
            s.id, s.filme.id, s.filme.titulo, s.sala.id, s.sala.nome,
            s.dataHora, s.idioma, cast(s.formato as string), s.preco, cast(s.status as string),
            cast((SELECT COUNT(sa) FROM SessaoAssentoJpaEntity sa
             WHERE sa.sessao.id = s.id AND sa.status = 'DISPONIVEL') as int)
        )
        FROM SessaoJpaEntity s
        WHERE s.filme.id = :filmeId AND s.status = 'ATIVA'
        ORDER BY s.dataHora ASC
        """)
    List<SessaoResult> findAtivasByFilmeId(@Param("filmeId") Long filmeId);

    @Query("""
        SELECT new com.cinesystem.application.sessao.dto.AssentoResult(
            sa.assento.id, sa.assento.fileira, sa.assento.numero,
            cast(sa.assento.tipo as string), cast(sa.status as string)
        )
        FROM SessaoAssentoJpaEntity sa
        WHERE sa.sessao.id = :sessaoId
        ORDER BY sa.assento.fileira ASC, sa.assento.numero ASC
        """)
    List<AssentoResult> findAssentosBySessaoId(@Param("sessaoId") Long sessaoId);
}
