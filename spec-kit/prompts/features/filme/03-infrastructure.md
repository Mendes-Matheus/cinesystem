---
context:
  - docs/architecture/layer-rules.md
  - docs/features/filme.md
  - docs/database/tables.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar infrastructure/persistence/filme

Implemente os 5 arquivos do adaptador de persistência do módulo Filme.
As interfaces `FilmeRepository` e `FilmeQueryPort` já existem.

## FilmeJpaEntity.java
- `@Entity @Table(name = "filme")`
- Campos espelham EXATAMENTE as colunas da tabela `filme` em `docs/database/tables.md`
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
- Enums mapeados como `@Enumerated(EnumType.STRING)`
- Sem lógica de negócio — apenas mapeamento JPA
- Lombok `@Getter @Setter @NoArgsConstructor`

## FilmeJpaRepository.java
- `interface FilmeJpaRepository extends JpaRepository<FilmeJpaEntity, Long>`
- Método de leitura com projeção JPQL:
  ```java
  @Query("""
      SELECT new com.cinesystem.application.filme.dto.FilmeResult(
          f.id, f.titulo, f.genero, f.classificacao,
          f.duracaoMinutos, f.posterUrl, f.dataLancamento
      )
      FROM FilmeJpaEntity f
      WHERE f.ativo = true
        AND (:genero IS NULL OR UPPER(f.genero) = UPPER(:genero))
      ORDER BY f.dataLancamento DESC
      """)
  List<FilmeResult> findProjectedAtivos(@Param("genero") String genero);

  @Query("SELECT new com.cinesystem.application.filme.dto.FilmeResult(...) FROM FilmeJpaEntity f WHERE f.id = :id")
  Optional<FilmeResult> findProjectedById(@Param("id") Long id);
  ```
- Método de escrita (carrega entidade completa): `List<FilmeJpaEntity> findByAtivoTrue()`

## FilmeJpaMapper.java
- `@Component`
- `Filme toDomainEntity(FilmeJpaEntity entity)`
- `FilmeJpaEntity toJpaEntity(Filme filme)`

## FilmeRepositoryAdapter.java
- `@Repository implements FilmeRepository`
- Injeta `FilmeJpaRepository` e `FilmeJpaMapper`
- Implementa os 4 métodos da interface delegando para o JPA

## FilmeQueryAdapter.java
- `@Repository implements FilmeQueryPort`
- Injeta apenas `FilmeJpaRepository`
- `findAllAtivos(genero)` → delega para `findProjectedAtivos(genero)`
- `findResultById(id)` → delega para `findProjectedById(id.valor())`
- ATENÇÃO: esta classe NUNCA instancia `Filme` — retorna `FilmeResult` diretamente

## Checklist

- [ ] `FilmeRepositoryAdapter` implementa `FilmeRepository` (domínio)
- [ ] `FilmeQueryAdapter` implementa `FilmeQueryPort` (aplicação)
- [ ] `FilmeJpaEntity` não estende nenhuma classe de domínio
- [ ] `FilmeQueryAdapter` não usa `FilmeJpaMapper` (não precisa — retorna projeção)
