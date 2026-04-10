package com.cinesystem.domain.filme;

import com.cinesystem.domain.shared.AggregateRoot;
import com.cinesystem.domain.shared.DomainException;

import java.time.LocalDate;
import java.util.Objects;

public class Filme extends AggregateRoot {

    private FilmeId id;
    private String titulo;
    private String sinopse;
    private Genero genero;
    private ClassificacaoEtaria classificacao;
    private int duracaoMinutos;
    private String posterUrl;
    private LocalDate dataLancamento;
    private boolean ativo;

    public Filme(FilmeId id, String titulo, String sinopse, Genero genero,
                 ClassificacaoEtaria classificacao, int duracaoMinutos,
                 String posterUrl, LocalDate dataLancamento, boolean ativo) {

        if (titulo == null || titulo.trim().isEmpty()) {
            throw new DomainException("Título obrigatório");
        }
        if (duracaoMinutos <= 0) {
            throw new DomainException("Duração deve ser maior que zero");
        }

        this.id = id;
        this.titulo = titulo;
        this.sinopse = sinopse;
        this.genero = genero;
        this.classificacao = classificacao;
        this.duracaoMinutos = duracaoMinutos;
        this.posterUrl = posterUrl;
        this.dataLancamento = dataLancamento;
        this.ativo = ativo;
    }

    public void desativar() {
        if (!this.ativo) {
            throw new DomainException("Filme já inativo");
        }
        this.ativo = false;
    }

    public FilmeId getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSinopse() {
        return sinopse;
    }

    public Genero getGenero() {
        return genero;
    }

    public ClassificacaoEtaria getClassificacao() {
        return classificacao;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filme filme = (Filme) o;
        return Objects.equals(id, filme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Filme{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
