package com.cinesystem.infrastructure.persistence.usuario;

import com.cinesystem.application.usuario.dto.UsuarioResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT new com.cinesystem.application.usuario.dto.UsuarioResult(
            u.id, u.nome, u.email, cast(u.role as string), u.ativo, u.criadoEm
        )
        FROM UsuarioJpaEntity u
        ORDER BY u.criadoEm DESC
        """)
    Page<UsuarioResult> findAllProjected(Pageable pageable);

    @Query("""
        SELECT new com.cinesystem.application.usuario.dto.UsuarioResult(
            u.id, u.nome, u.email, cast(u.role as string), u.ativo, u.criadoEm
        )
        FROM UsuarioJpaEntity u
        WHERE u.id = :id
        """)
    Optional<UsuarioResult> findProjectedById(@Param("id") Long id);
}
