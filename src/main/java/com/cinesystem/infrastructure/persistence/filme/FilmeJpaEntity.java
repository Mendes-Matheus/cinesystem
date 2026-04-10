package com.cinesystem.infrastructure.persistence.filme;

import com.cinesystem.domain.filme.Genero;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "filme")
@Getter
@Setter
@NoArgsConstructor
public class FilmeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private String sinopse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(nullable = false)
    private String classificacao;

    @Column(name = "duracao_min", nullable = false)
    private int duracaoMinutos;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(nullable = false)
    private boolean ativo = true;
}
