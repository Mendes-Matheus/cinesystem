package com.cinesystem.domain.sessao;

import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.shared.AggregateRoot;
import com.cinesystem.domain.shared.DomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Sessao extends AggregateRoot {
    private SessaoId id;
    private FilmeId filmeId;
    private SalaId salaId;
    private LocalDateTime dataHora;
    private String idioma;
    private FormatoExibicao formato;
    private BigDecimal preco;
    private StatusSessao status;

    public Sessao(SessaoId id, FilmeId filmeId, SalaId salaId, LocalDateTime dataHora, 
                  String idioma, FormatoExibicao formato, BigDecimal preco, StatusSessao status) {
        if (dataHora == null || dataHora.isBefore(LocalDateTime.now())) {
            throw new DomainException("Sessão deve ser agendada no futuro");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Preço deve ser maior que zero");
        }
        this.id = id;
        this.filmeId = filmeId;
        this.salaId = salaId;
        this.dataHora = dataHora;
        this.idioma = idioma;
        this.formato = formato;
        this.preco = preco;
        this.status = status;
    }

    public void cancelar() {
        if (this.status != StatusSessao.ATIVA) {
            throw new DomainException("Apenas sessões ATIVAs podem ser canceladas");
        }
        this.status = StatusSessao.CANCELADA;
    }

    public SessaoId getId() { return id; }
    public FilmeId getFilmeId() { return filmeId; }
    public SalaId getSalaId() { return salaId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getIdioma() { return idioma; }
    public FormatoExibicao getFormato() { return formato; }
    public BigDecimal getPreco() { return preco; }
    public StatusSessao getStatus() { return status; }
}
