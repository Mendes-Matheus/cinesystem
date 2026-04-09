---
context:
  - docs/architecture/layer-rules.md
  - docs/features/sessao.md
  - docs/database/tables.md
---

# Tarefa: Implementar infrastructure/persistence/sessao e assento

## infrastructure/persistence/assento/

### AssentoJpaEntity.java
- `@Entity @Table(name = "assento")`
- Campos: id, salaId (FK), fileira, numero, tipo
- `@ManyToOne @JoinColumn(name = "sala_id")` para relação com SalaJpaEntity

### AssentoJpaRepository.java
- `findBySalaId(Long salaId): List<AssentoJpaEntity>`

### AssentoRepositoryAdapter.java
- `implements AssentoRepository`

### AssentoJpaMapper.java
- `Assento toDomainEntity(AssentoJpaEntity)`
- `AssentoJpaEntity toJpaEntity(Assento)`

---

## infrastructure/persistence/sessao/

### SessaoJpaEntity.java
- `@Entity @Table(name = "sessao")`
- `@ManyToOne` para FilmeJpaEntity e SalaJpaEntity

### SessaoAssentoJpaEntity.java
- `@Entity @Table(name = "sessao_assento")`
- `@ManyToOne` para SessaoJpaEntity e AssentoJpaEntity
- `@ManyToOne(optional = true)` para UsuarioJpaEntity

### SessaoJpaRepository.java
- Projeção para CQRS:
  ```java
  @Query("""
      SELECT new com.cinesystem.application.sessao.dto.SessaoResult(
          s.id, s.filme.id, s.filme.titulo, s.sala.id, s.sala.nome,
          s.dataHora, s.idioma, s.formato, s.preco, s.status,
          (SELECT COUNT(sa) FROM SessaoAssentoJpaEntity sa
           WHERE sa.sessao.id = s.id AND sa.status = 'DISPONIVEL')
      )
      FROM SessaoJpaEntity s
      WHERE s.filme.id = :filmeId AND s.status = 'ATIVA'
      ORDER BY s.dataHora ASC
      """)
  List<SessaoResult> findAtivasByFilmeId(@Param("filmeId") Long filmeId);

  @Query("""
      SELECT new com.cinesystem.application.sessao.dto.AssentoResult(
          sa.assento.id, sa.assento.fileira, sa.assento.numero,
          sa.assento.tipo, sa.status
      )
      FROM SessaoAssentoJpaEntity sa
      WHERE sa.sessao.id = :sessaoId
      ORDER BY sa.assento.fileira ASC, sa.assento.numero ASC
      """)
  List<AssentoResult> findAssentosBySessaoId(@Param("sessaoId") Long sessaoId);
  ```

### SessaoRepositoryAdapter.java
- `implements SessaoRepository`
- `findSessaoAssento(sessaoId, assentoId)`:
  ```java
  sessaoAssentoJpaRepository
      .findBySessaoIdAndAssentoId(sessaoId.valor(), assentoId.valor())
      .map(mapper::toDomainSessaoAssento)
  ```

### SessaoQueryAdapter.java
- `implements SessaoQueryPort`
- Delega para projeções do `SessaoJpaRepository`

### SessaoJpaMapper.java
- Mapeia `Sessao ↔ SessaoJpaEntity` e `SessaoAssento ↔ SessaoAssentoJpaEntity`

## Checklist

- [ ] `SessaoQueryAdapter` nunca instancia `Sessao` — retorna projeções
- [ ] Subquery de `assentosDisponiveis` está na query JPQL do `findAtivasByFilmeId`
- [ ] `SessaoAssentoJpaEntity.usuarioId` é nullable
