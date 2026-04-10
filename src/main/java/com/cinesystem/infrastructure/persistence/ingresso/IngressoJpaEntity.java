package com.cinesystem.infrastructure.persistence.ingresso;

import com.cinesystem.domain.ingresso.StatusIngresso;
import com.cinesystem.infrastructure.persistence.sessao.SessaoAssentoJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingresso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngressoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(36)", nullable = false)
    private String codigo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_assento_id", nullable = false)
    private SessaoAssentoJpaEntity sessaoAssento;

    @Column(name = "valor_pago", nullable = false)
    private BigDecimal valorPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusIngresso status;

    @Column(name = "comprado_em", nullable = false)
    private LocalDateTime compradoEm;
}
