package com.cinesystem.infrastructure.persistence.sessao;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;
import com.cinesystem.infrastructure.persistence.assento.AssentoJpaEntity;
import com.cinesystem.infrastructure.persistence.filme.FilmeJpaEntity;
import com.cinesystem.infrastructure.persistence.sala.SalaJpaEntity;
import com.cinesystem.infrastructure.persistence.usuario.UsuarioJpaEntity;
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
