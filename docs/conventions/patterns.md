# Padrões Obrigatórios

## 1. Value Objects como record

Todo VO valida no compact constructor e lança `DomainException`.
```java
public record FilmeId(Long valor) {
    public FilmeId { Objects.requireNonNull(valor, "FilmeId não pode ser nulo"); }
}
```

## 2. Commands e Results como record

Sempre imutáveis. Nunca classes com setters.
```java
public record CriarFilmeCommand(String titulo, Genero genero, ...) {}
public record FilmeResult(Long id, String titulo, ...) {
    public static FilmeResult from(Filme f) { return new FilmeResult(f.getId().valor(), ...); }
}
```

## 3. CQRS tático — QueryPort para listagens

Use cases de leitura NUNCA carregam entidade de domínio.
```
Leitura  → QueryPort  → JPQL projetado → Result (sem Filme na memória)
Escrita  → Repository → Entidade       → save()
```

## 4. Outbox Pattern — eventos transacionais

Para qualquer evento que dispara e-mail, push ou integração externa
após uma transação de banco, usar Outbox. Nunca `ApplicationEventPublisher` nesses casos.
```java
// Correto
outboxRepository.save(OutboxEvent.of("IngressoComprado", id, payload)); // mesma @Transactional

// Errado — pode perder o evento se a JVM morrer após o commit
publisher.publishEvent(new IngressoCompradoEvent(salvo));
```

## 5. Adaptador implementa UMA porta

Nunca implementar duas interfaces em um mesmo adaptador.
```java
// Correto
class FilmeRepositoryAdapter implements FilmeRepository { ... }
class FilmeQueryAdapter implements FilmeQueryPort { ... }

// Errado
class FilmeAdapter implements FilmeRepository, FilmeQueryPort { ... }
```

## 6. Controller injeta interface, nunca implementação

```java
// Correto
private final CriarFilmeUseCase criarFilme;

// Errado
private final CriarFilmeUseCaseImpl criarFilme;
private final FilmeRepositoryAdapter filmeAdapter;
```

## 7. Nomenclatura de arquivos Flyway

```
V1__create_core_tables.sql
V2__create_outbox.sql
V3__create_indexes.sql
V4__seed_admin_user.sql
```

## 8. Strategy Pattern para pagamentos

```java
public interface PagamentoStrategy {
    PagamentoResponseDTO processar(PagamentoRequestDTO request);
}
@Component("PIX")      class PixStrategy    implements PagamentoStrategy { ... }
@Component("CARTAO")   class CartaoStrategy implements PagamentoStrategy { ... }

// PagamentoService injeta Map<String, PagamentoStrategy>
var strategy = strategies.get(dto.getMetodo().name());
```
