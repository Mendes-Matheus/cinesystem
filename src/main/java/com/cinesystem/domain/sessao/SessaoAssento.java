package com.cinesystem.domain.sessao;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.assento.StatusAssento;
import com.cinesystem.domain.ingresso.CodigoIngresso;
import com.cinesystem.domain.ingresso.Ingresso;
import com.cinesystem.domain.ingresso.StatusIngresso;
import com.cinesystem.domain.ingresso.TipoIngresso;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.UsuarioId;

import java.math.BigDecimal;
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
            throw new DomainException("Assento já reservado");
        }
    }

    private String reservaIdentificador; // Novo campo para UUID de visitante ou ID de usuário

    public void reservarTemporariamente(String identificador, int tempoMinutos) {
        if (this.status != StatusAssento.DISPONIVEL) {
            throw new DomainException("Assento não está disponível");
        }
        this.status = StatusAssento.RESERVADO;
        this.reservaIdentificador = identificador;
        this.reservadoAte = LocalDateTime.now().plusMinutes(tempoMinutos);
    }

    // O método confirmarCompra agora exige o identificador que realizou a reserva
    public Ingresso iniciarFluxoCompra(String identificador, TipoIngresso tipo, BigDecimal precoSessao) {
        if (this.status != StatusAssento.RESERVADO || !this.reservaIdentificador.equals(identificador)) {
            throw new DomainException("Reserva inválida ou expirada");
        }

        BigDecimal valorFinal = tipo == TipoIngresso.MEIA ? precoSessao.divide(new BigDecimal("2")) : precoSessao;

        return new Ingresso(
                null,
                CodigoIngresso.gerar(),
                null, // Será preenchido após o login no checkout
                this.id,
                valorFinal,
                StatusIngresso.AGUARDANDO_PAGAMENTO,
                LocalDateTime.now()
        );
    }

    public void efetivarOcupacao() {
        if (this.status != StatusAssento.RESERVADO) {
            throw new DomainException("O assento precisa estar reservado para ser ocupado definitivamente.");
        }
        this.status = StatusAssento.OCUPADO;
        this.reservadoAte = null; // Limpa o tempo de expiração
    }

    public void liberarReservaExpirada() {
        if (this.status == StatusAssento.RESERVADO) {
            this.status = StatusAssento.DISPONIVEL;
            this.reservadoAte = null;
            this.usuarioId = null;
        }
    }

    public Long getId() { return id; }
    public SessaoId getSessaoId() { return sessaoId; }
    public AssentoId getAssentoId() { return assentoId; }
    public StatusAssento getStatus() { return status; }
    public LocalDateTime getReservadoAte() { return reservadoAte; }
    public UsuarioId getUsuarioId() { return usuarioId; }


}
