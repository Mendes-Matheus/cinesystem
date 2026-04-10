package com.cinesystem.infrastructure.persistence.ingresso;

import com.cinesystem.domain.ingresso.CodigoIngresso;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.usuario.UsuarioId;
import com.cinesystem.infrastructure.persistence.sessao.SessaoAssentoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class IngressoJpaMapper {

    public Ingresso toDomainEntity(IngressoJpaEntity entity) {
        if (entity == null) return null;
        return new Ingresso(
                new IngressoId(entity.getId()),
                new CodigoIngresso(entity.getCodigo()),
                new UsuarioId(entity.getUsuarioId()),
                entity.getSessaoAssento() != null ? entity.getSessaoAssento().getId() : null,
                entity.getValorPago(),
                entity.getStatus(),
                entity.getCompradoEm()
        );
    }

    public IngressoJpaEntity toJpaEntity(Ingresso ingresso) {
        if (ingresso == null) return null;
        IngressoJpaEntity entity = new IngressoJpaEntity();
        if (ingresso.getId() != null) {
             entity.setId(ingresso.getId().id());
        }
        if (ingresso.getCodigo() != null) {
             entity.setCodigo(ingresso.getCodigo().valor());
        }
        if (ingresso.getUsuarioId() != null) {
            entity.setUsuarioId(ingresso.getUsuarioId().valor());
        }
        if (ingresso.getSessaoAssentoId() != null) {
            SessaoAssentoJpaEntity sessaoAssento = new SessaoAssentoJpaEntity();
            sessaoAssento.setId(ingresso.getSessaoAssentoId());
            entity.setSessaoAssento(sessaoAssento);
        }
        entity.setValorPago(ingresso.getValorPago());
        entity.setStatus(ingresso.getStatus());
        entity.setCompradoEm(ingresso.getCompradoEm());
        return entity;
    }
}
