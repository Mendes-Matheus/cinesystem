package com.cinesystem.infrastructure.persistence.sessao;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface SessaoAssentoJpaRepository extends JpaRepository<SessaoAssentoJpaEntity, Long> {
    Optional<SessaoAssentoJpaEntity> findBySessaoIdAndAssentoId(Long sessaoId, Long assentoId);
    List<SessaoAssentoJpaEntity> findBySessaoId(Long sessaoId);

    @Query("SELECT sa FROM SessaoAssentoJpaEntity sa WHERE sa.status = 'RESERVADO' AND sa.reservadoAte < :dataLimite")
    List<SessaoAssentoJpaEntity> findReservasExpiradas(@Param("dataLimite") java.time.LocalDateTime dataLimite);

    @Query("SELECT sa FROM SessaoAssentoJpaEntity sa " +
            "JOIN FETCH sa.assento " +
            "WHERE sa.id = :id")
    Optional<SessaoAssentoJpaEntity> findByIdWithAssento(Long id);
}
