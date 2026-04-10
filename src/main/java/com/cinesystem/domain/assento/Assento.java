package com.cinesystem.domain.assento;

import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.shared.DomainException;

public class Assento {
    private AssentoId id;
    private SalaId salaId;
    private String fileira;
    private int numero;
    private TipoAssento tipo;

    public Assento(AssentoId id, SalaId salaId, String fileira, int numero, TipoAssento tipo) {
        if (fileira == null || fileira.trim().isEmpty()) {
            throw new DomainException("Fileira obrigatória");
        }
        if (numero <= 0) {
            throw new DomainException("Número deve ser maior que zero");
        }
        this.id = id;
        this.salaId = salaId;
        this.fileira = fileira;
        this.numero = numero;
        this.tipo = tipo;
    }
    
    public AssentoId getId() { return id; }
    public SalaId getSalaId() { return salaId; }
    public String getFileira() { return fileira; }
    public int getNumero() { return numero; }
    public TipoAssento getTipo() { return tipo; }
}
