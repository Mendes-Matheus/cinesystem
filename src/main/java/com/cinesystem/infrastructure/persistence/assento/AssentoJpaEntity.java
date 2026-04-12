package com.cinesystem.infrastructure.persistence.assento;

import com.cinesystem.domain.assento.TipoAssento;
import com.cinesystem.infrastructure.persistence.sala.SalaJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assento")
@Getter
@Setter
@NoArgsConstructor
public class AssentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private SalaJpaEntity sala;

    @Column(nullable = false, length = 1)
    private String fileira;

    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAssento tipo;
}
