package com.cinesystem.infrastructure.persistence.sessao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SessaoAssentoJpaRepository extends JpaRepository<SessaoAssentoJpaEntity, Long> {
    Optional<SessaoAssentoJpaEntity> findBySessaoIdAndAssentoId(Long sessaoId, Long assentoId);
    List<SessaoAssentoJpaEntity> findBySessaoId(Long sessaoId);
}
