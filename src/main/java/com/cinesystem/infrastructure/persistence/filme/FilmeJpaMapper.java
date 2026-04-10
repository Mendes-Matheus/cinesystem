package com.cinesystem.infrastructure.persistence.filme;

import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeId;
import org.springframework.stereotype.Component;

@Component
public class FilmeJpaMapper {

    public Filme toDomainEntity(FilmeJpaEntity entity) {
        if (entity == null) return null;
        return new Filme(
                new FilmeId(entity.getId()),
                entity.getTitulo(),
                entity.getSinopse(),
                entity.getGenero(),
                ClassificacaoEtaria.of(entity.getClassificacao()),
                entity.getDuracaoMinutos(),
                entity.getPosterUrl(),
                entity.getDataLancamento(),
                entity.isAtivo()
        );
    }

    public FilmeJpaEntity toJpaEntity(Filme filme) {
        if (filme == null) return null;
        FilmeJpaEntity entity = new FilmeJpaEntity();
        if (filme.getId() != null) {
            entity.setId(filme.getId().valor());
        }
        entity.setTitulo(filme.getTitulo());
        entity.setSinopse(filme.getSinopse());
        entity.setGenero(filme.getGenero());
        entity.setClassificacao(filme.getClassificacao().codigo());
        entity.setDuracaoMinutos(filme.getDuracaoMinutos());
        entity.setPosterUrl(filme.getPosterUrl());
        entity.setDataLancamento(filme.getDataLancamento());
        entity.setAtivo(filme.isAtivo());
        return entity;
    }
}
