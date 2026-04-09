---
context:
  - docs/architecture/layer-rules.md
  - docs/features/ingresso.md
  - docs/database/tables.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar infrastructure — módulo Ingresso

Implemente os adaptadores de persistência e cache para o módulo Ingresso.
As interfaces `IngressoRepository`, `ReservaAssentoPort` e `OutboxRepository` já existem.

## infrastructure/persistence/ingresso/

### IngressoJpaEntity.java
- `@Entity @Table(name = "ingresso")`
- Campos espelham EXATAMENTE as colunas da tabela `ingresso` em `docs/database/tables.md`
- `codigo` é VARCHAR(36) — armazena o valor de `CodigoIngresso`
- Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`

### IngressoJpaRepository.java
- `interface extends JpaRepository<IngressoJpaEntity, Long>`
- Método: `Optional<IngressoJpaEntity> findByCodigo(String codigo)`
- Projeção de leitura (CQRS):
  ```java
  @Query("""
      SELECT new com.cinesystem.application.ingresso.dto.IngressoResult(
          i.id, i.codigo, sa.sessao.id, sa.assento.id,
          sa.assento.fileira, sa.assento.numero,
          s.filme.titulo, s.dataHora, i.valorPago, i.status
      )
      FROM IngressoJpaEntity i
      JOIN i.sessaoAssento sa
      JOIN sa.sessao s
      WHERE i.usuarioId = :usuarioId
      ORDER BY i.compradoEm DESC
      """)
  List<IngressoResult> findProjectedByUsuarioId(@Param("usuarioId") Long usuarioId);
  ```

### IngressoJpaMapper.java
- `@Component`
- `Ingresso toDomainEntity(IngressoJpaEntity entity)`
- `IngressoJpaEntity toJpaEntity(Ingresso ingresso)`

### IngressoRepositoryAdapter.java
- `@Repository implements IngressoRepository`
- Implementa os 3 métodos da interface delegando para o JPA + mapper

### IngressoQueryAdapter.java
- `@Repository implements IngressoQueryPort`
- `findByUsuario(UsuarioId)` → delega para `findProjectedByUsuarioId(id.valor())`
- NUNCA instancia `Ingresso` — retorna `IngressoResult` diretamente

---

## infrastructure/persistence/outbox/

### OutboxEventJpaEntity.java
- `@Entity @Table(name = "outbox_events")`
- Campos espelham EXATAMENTE as colunas da tabela `outbox_events` em `docs/database/tables.md`
- `payload` mapeado como `@Column(columnDefinition = "jsonb")`
- Lombok `@Getter @Setter @NoArgsConstructor`

### OutboxJpaRepository.java
- `interface extends JpaRepository<OutboxEventJpaEntity, Long>`
- ```java
  @Query("""
      SELECT o FROM OutboxEventJpaEntity o
      WHERE o.status = 'PENDENTE'
      ORDER BY o.criadoEm ASC
      LIMIT :limit
      """)
  List<OutboxEventJpaEntity> findPendentes(@Param("limit") int limit);
  ```

### OutboxRepositoryAdapter.java
- `@Repository implements OutboxRepository`
- `save(OutboxEvent)` → converte para JpaEntity, salva, converte de volta
- `findPendentes(limit)` → delega para JPA, converte lista

---

## infrastructure/cache/

### RedisReservaAdapter.java
- `@Component implements ReservaAssentoPort`
- Injeta `RedisTemplate<String, Object>`
- Chave: `"reserva:%d:%d".formatted(sessaoId.valor(), assentoId.valor())`
- TTL: 10 minutos
- `reservar()`: usa `setIfAbsent()` para atomicidade (SETNX)
- `liberar()`: usa `delete()`

## Checklist

- [ ] `IngressoQueryAdapter` não usa mapper — retorna projeção diretamente
- [ ] `OutboxEventJpaEntity.payload` tem `columnDefinition = "jsonb"`
- [ ] `RedisReservaAdapter` usa `setIfAbsent` (não `set`)
- [ ] Nenhum adaptador contém lógica de negócio
