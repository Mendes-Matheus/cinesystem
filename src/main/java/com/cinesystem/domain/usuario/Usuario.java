package com.cinesystem.domain.usuario;

import com.cinesystem.domain.shared.DomainException;

public class Usuario {
    private UsuarioId id;
    private String nome;
    private Email email;
    private Senha senha;
    private Role role;
    private boolean ativo;

    public Usuario(UsuarioId id, String nome, Email email, Senha senha, Role role, boolean ativo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome não pode ser vazio");
        }
        if (email == null) {
            throw new DomainException("Email não pode ser nulo");
        }
        if (senha == null) {
            throw new DomainException("Senha não pode ser nula");
        }
        if (role == null) {
            throw new DomainException("Role não pode ser nula");
        }
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.ativo = ativo;
    }

    public void desativar() {
        if (!this.ativo) {
            throw new DomainException("Conta já desativada");
        }
        this.ativo = false;
    }

    public UsuarioId getId() { return id; }
    public String getNome() { return nome; }
    public Email getEmail() { return email; }
    public Senha getSenha() { return senha; }
    public Role getRole() { return role; }
    public boolean isAtivo() { return ativo; }
}
