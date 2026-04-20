package com.cinesystem.infrastructure.persistence.usuario;

import com.cinesystem.domain.usuario.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate; // Adicionado
import org.springframework.data.annotation.LastModifiedDate; // Adicionado
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Adicionado

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class) // Adicionado para ouvir eventos de auditoria
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean ativo;

    @CreatedDate // Gerido automaticamente pelo Spring Data no INSERT
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate // Gerido automaticamente pelo Spring Data no UPDATE
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    // Os métodos onCreate() e onUpdate() manuais foram removidos
    // pois a AuditingEntityListener agora cuida disso.
}
