package com.cinesystem.domain.sessao;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.assento.StatusAssento;
import com.cinesystem.domain.ingresso.CodigoIngresso;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.UsuarioId;

import java.time.LocalDateTime;

public class SessaoAssento {
    private Long id;
    private SessaoId sessaoId;
    private AssentoId assentoId;
    private StatusAssento status;
    private LocalDateTime reservadoAte;
    private UsuarioId usuarioId;

    public SessaoAssento(Long id, SessaoId sessaoId, AssentoId assentoId, StatusAssento status, LocalDateTime reservadoAte, UsuarioId usuarioId) {
        this.id = id;
        this.sessaoId = sessaoId;
        this.assentoId = assentoId;
        this.status = status;
        this.reservadoAte = reservadoAte;
        this.usuarioId = usuarioId;
    }

    public Ingresso confirmarCompra(UsuarioId confirmadorId, Sessao sessao) {
        if (this.status == StatusAssento.DISPONIVEL || 
           (this.status == StatusAssento.RESERVADO && this.usuarioId != null && this.usuarioId.equals(confirmadorId))) {
            
            this.status = StatusAssento.OCUPADO;
            this.usuarioId = confirmadorId;
            return new Ingresso(
                    null,
                    CodigoIngresso.gerar(),
                    confirmadorId,
                    this.id,
                    sessao.getPreco(),
                    null,
                    null
            );
        } else {
            throw new DomainException("Assento não disponível para este usuário");
        }
    }

    public Long getId() { return id; }
    public SessaoId getSessaoId() { return sessaoId; }
    public AssentoId getAssentoId() { return assentoId; }
    public StatusAssento getStatus() { return status; }
    public LocalDateTime getReservadoAte() { return reservadoAte; }
    public UsuarioId getUsuarioId() { return usuarioId; }
}
