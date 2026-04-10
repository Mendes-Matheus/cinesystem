package com.cinesystem.infrastructure.persistence.assento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssentoJpaRepository extends JpaRepository<AssentoJpaEntity, Long> {
    List<AssentoJpaEntity> findBySalaId(Long salaId);
}
