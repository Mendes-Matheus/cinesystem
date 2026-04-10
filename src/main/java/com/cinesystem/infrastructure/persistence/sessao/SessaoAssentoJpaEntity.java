package com.cinesystem.infrastructure.persistence.sessao;

import com.cinesystem.domain.assento.StatusAssento;
import com.cinesystem.infrastructure.persistence.assento.AssentoJpaEntity;
import com.cinesystem.infrastructure.persistence.usuario.UsuarioJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessao_assento")
@Getter
@Setter
@NoArgsConstructor
public class SessaoAssentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoJpaEntity sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assento_id", nullable = false)
    private AssentoJpaEntity assento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAssento status;

    @Column(name = "reservado_ate")
    private LocalDateTime reservadoAte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private UsuarioJpaEntity usuario;
}
