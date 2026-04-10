package com.cinesystem.infrastructure.persistence.filme;

import com.cinesystem.application.filme.dto.FilmeResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilmeJpaRepository extends JpaRepository<FilmeJpaEntity, Long> {

    @Query("""
        SELECT new com.cinesystem.application.filme.dto.FilmeResult(
            f.id, f.titulo, cast(f.genero as string), f.classificacao,
            f.duracaoMinutos, f.posterUrl, f.dataLancamento
        )
        FROM FilmeJpaEntity f
        WHERE f.ativo = true
          AND (:genero IS NULL OR UPPER(cast(f.genero as string)) = UPPER(:genero))
        ORDER BY f.dataLancamento DESC
        """)
    List<FilmeResult> findProjectedAtivos(@Param("genero") String genero);

    @Query("""
        SELECT new com.cinesystem.application.filme.dto.FilmeResult(
            f.id, f.titulo, cast(f.genero as string), f.classificacao,
            f.duracaoMinutos, f.posterUrl, f.dataLancamento
        )
        FROM FilmeJpaEntity f 
        WHERE f.id = :id
        """)
    Optional<FilmeResult> findProjectedById(@Param("id") Long id);

    List<FilmeJpaEntity> findByAtivoTrue();
}
