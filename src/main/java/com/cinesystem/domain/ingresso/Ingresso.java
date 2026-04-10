package com.cinesystem.domain.ingresso;

import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.UsuarioId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Ingresso {
    private IngressoId id;
    private CodigoIngresso codigo;
    private UsuarioId usuarioId;
    private Long sessaoAssentoId;
    private BigDecimal valorPago;
    private StatusIngresso status;
    private LocalDateTime compradoEm;

    public Ingresso(IngressoId id, CodigoIngresso codigo, UsuarioId usuarioId, Long sessaoAssentoId, BigDecimal valorPago, StatusIngresso status, LocalDateTime compradoEm) {
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Valor do ingresso deve ser positivo");
        }
        this.id = id;
        this.codigo = codigo != null ? codigo : CodigoIngresso.gerar();
        this.usuarioId = usuarioId;
        this.sessaoAssentoId = sessaoAssentoId;
        this.valorPago = valorPago;
        this.status = status != null ? status : StatusIngresso.ATIVO;
        this.compradoEm = compradoEm != null ? compradoEm : LocalDateTime.now();
    }

    public void cancelar() {
        if (this.status != StatusIngresso.ATIVO) {
            throw new DomainException("Ingresso não pode ser cancelado: status " + this.status);
        }
        this.status = StatusIngresso.CANCELADO;
    }

    public void marcarUtilizado() {
        if (this.status != StatusIngresso.ATIVO) {
            throw new DomainException("Ingresso já foi utilizado ou cancelado");
        }
        this.status = StatusIngresso.UTILIZADO;
    }

    public IngressoId getId() { return id; }
    public CodigoIngresso getCodigo() { return codigo; }
    public UsuarioId getUsuarioId() { return usuarioId; }
    public Long getSessaoAssentoId() { return sessaoAssentoId; }
    public BigDecimal getValorPago() { return valorPago; }
    public StatusIngresso getStatus() { return status; }
    public LocalDateTime getCompradoEm() { return compradoEm; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingresso ingresso = (Ingresso) o;
        return Objects.equals(id, ingresso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
