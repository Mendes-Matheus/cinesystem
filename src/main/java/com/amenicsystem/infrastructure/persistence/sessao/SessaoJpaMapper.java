package com.amenicsystem.infrastructure.persistence.sessao;

import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.filme.FilmeId;
import com.amenicsystem.domain.sala.SalaId;
import com.amenicsystem.domain.sessao.Sessao;
import com.amenicsystem.domain.sessao.SessaoAssento;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;
import com.amenicsystem.infrastructure.persistence.assento.AssentoJpaEntity;
import com.amenicsystem.infrastructure.persistence.filme.FilmeJpaEntity;
import com.amenicsystem.infrastructure.persistence.sala.SalaJpaEntity;
import com.amenicsystem.infrastructure.persistence.usuario.UsuarioJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SessaoJpaMapper {

    public Sessao toDomainSessao(SessaoJpaEntity entity) {
        if (entity == null) return null;
        return new Sessao(
                new SessaoId(entity.getId()),
                new FilmeId(entity.getFilme().getId()),
                new SalaId(entity.getSala().getId()),
                entity.getDataHora(),
                entity.getIdioma(),
                entity.getFormato(),
                entity.getPreco(),
                entity.getStatus()
        );
    }

    public SessaoJpaEntity toJpaSessao(Sessao sessao) {
        if (sessao == null) return null;
        SessaoJpaEntity entity = new SessaoJpaEntity();
        if (sessao.getId() != null) {
            entity.setId(sessao.getId().id());
        }
        FilmeJpaEntity filme = new FilmeJpaEntity();
        if (sessao.getFilmeId() != null) {
            filme.setId(sessao.getFilmeId().id());
        }
        entity.setFilme(filme);

        SalaJpaEntity sala = new SalaJpaEntity();
        if (sessao.getSalaId() != null) {
            sala.setId(sessao.getSalaId().id());
        }
        entity.setSala(sala);

        entity.setDataHora(sessao.getDataHora());
        entity.setIdioma(sessao.getIdioma());
        entity.setFormato(sessao.getFormato());
        entity.setPreco(sessao.getPreco());
        entity.setStatus(sessao.getStatus());
        return entity;
    }

    public SessaoAssento toDomainSessaoAssento(SessaoAssentoJpaEntity entity) {
        if (entity == null) return null;
        return new SessaoAssento(
                entity.getId(),
                new SessaoId(entity.getSessao().getId()),
                new AssentoId(entity.getAssento().getId()),
                entity.getStatus(),
                entity.getReservadoAte(),
                entity.getUsuario() != null ? new UsuarioId(entity.getUsuario().getId()) : null,
                entity.getReservaIdentificador()
        );
    }

    public SessaoAssentoJpaEntity toJpaSessaoAssento(SessaoAssento sessaoAssento) {
        if (sessaoAssento == null) return null;
        SessaoAssentoJpaEntity entity = new SessaoAssentoJpaEntity();
        entity.setId(sessaoAssento.getId());
        
        SessaoJpaEntity sessao = new SessaoJpaEntity();
        if (sessaoAssento.getSessaoId() != null) {
            sessao.setId(sessaoAssento.getSessaoId().id());
        }
        entity.setSessao(sessao);

        AssentoJpaEntity assento = new AssentoJpaEntity();
        if (sessaoAssento.getAssentoId() != null) {
            assento.setId(sessaoAssento.getAssentoId().id());
        }
        entity.setAssento(assento);

        if (sessaoAssento.getUsuarioId() != null) {
            UsuarioJpaEntity usuario = new UsuarioJpaEntity();
            usuario.setId(sessaoAssento.getUsuarioId().id());
            entity.setUsuario(usuario);
        }

        entity.setStatus(sessaoAssento.getStatus());
        entity.setReservadoAte(sessaoAssento.getReservadoAte());
        entity.setReservaIdentificador(sessaoAssento.getReservaIdentificador());
        return entity;
    }
}
