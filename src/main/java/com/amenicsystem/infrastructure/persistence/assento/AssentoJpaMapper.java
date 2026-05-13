package com.amenicsystem.infrastructure.persistence.assento;

import com.amenicsystem.domain.assento.Assento;
import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.sala.SalaId;
import com.amenicsystem.infrastructure.persistence.sala.SalaJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AssentoJpaMapper {

    public Assento toDomainEntity(AssentoJpaEntity entity) {
        if (entity == null) return null;
        return new Assento(
                new AssentoId(entity.getId()),
                new SalaId(entity.getSala().getId()),
                entity.getFileira(),
                entity.getNumero(),
                entity.getTipo()
        );
    }

    public AssentoJpaEntity toJpaEntity(Assento assento) {
        if (assento == null) return null;
        AssentoJpaEntity entity = new AssentoJpaEntity();
        if (assento.getId() != null) {
            entity.setId(assento.getId().id());
        }
        SalaJpaEntity sala = new SalaJpaEntity();
        if (assento.getSalaId() != null) {
            sala.setId(assento.getSalaId().id()); 
        }
        entity.setSala(sala);
        entity.setFileira(assento.getFileira());
        entity.setNumero(assento.getNumero());
        entity.setTipo(assento.getTipo());
        return entity;
    }
}
