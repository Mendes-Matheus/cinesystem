package com.cinesystem.infrastructure.persistence.sessao;

import com.cinesystem.domain.sessao.FormatoExibicao;
import com.cinesystem.domain.sessao.StatusSessao;
import com.cinesystem.infrastructure.persistence.filme.FilmeJpaEntity;
import com.cinesystem.infrastructure.persistence.sala.SalaJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessao")
@Getter
@Setter
@NoArgsConstructor
public class SessaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filme_id", nullable = false)
    private FilmeJpaEntity filme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private SalaJpaEntity sala;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String idioma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatoExibicao formato;

    @Column(nullable = false)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSessao status;

}
