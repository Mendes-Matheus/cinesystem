# Feature: Filme

## Domínio

### Entidade: Filme
```
id              FilmeId         (Value Object — Long encapsulado)
titulo          String          NOT NULL, não vazio
sinopse         String          nullable
genero          Genero          enum
classificacao   ClassificacaoEtaria  Value Object (L, 10, 12, 14, 16, 18)
duracaoMinutos  int             > 0
posterUrl       String          nullable
dataLancamento  LocalDate       NOT NULL
ativo           boolean         default true
```

### Invariantes de domínio
- Título não pode ser nulo nem vazio → `DomainException("Título obrigatório")`
- Duração deve ser positiva → `DomainException("Duração deve ser maior que zero")`
- `desativar()` lança `DomainException("Filme já inativo")` se já estiver inativo

### Interface de repositório (domínio)
```java
public interface FilmeRepository {
    Filme save(Filme filme);
    Optional<Filme> findById(FilmeId id);
    void delete(FilmeId id);
}
```

---

## Application

### Use Cases
| Interface                    | Impl                          | Porta de entrada |
|------------------------------|-------------------------------|------------------|
| `ListarFilmesUseCase`        | `ListarFilmesUseCaseImpl`     | `execute(String genero): List<FilmeResult>` |
| `BuscarFilmePorIdUseCase`    | `BuscarFilmePorIdUseCaseImpl` | `execute(FilmeId): FilmeResult` |
| `CriarFilmeUseCase`          | `CriarFilmeUseCaseImpl`       | `execute(CriarFilmeCommand): FilmeResult` |
| `AtualizarFilmeUseCase`      | `AtualizarFilmeUseCaseImpl`   | `execute(AtualizarFilmeCommand): FilmeResult` |

### Commands (records)
```java
public record CriarFilmeCommand(
    String titulo, Genero genero, ClassificacaoEtaria classificacao,
    int duracaoMinutos, String posterUrl, LocalDate dataLancamento
) {}

public record AtualizarFilmeCommand(
    FilmeId id, String titulo, Genero genero, ClassificacaoEtaria classificacao,
    int duracaoMinutos, String posterUrl
) {}
```

### Result (record)
```java
public record FilmeResult(
    Long id, String titulo, String genero, String classificacao,
    int duracaoMinutos, String posterUrl, LocalDate dataLancamento
) {
    public static FilmeResult from(Filme filme) { ... }
}
```

### Porta de leitura (CQRS — não carrega entidade de domínio)
```java
public interface FilmeQueryPort {
    List<FilmeResult> findAllAtivos(String genero);
    Optional<FilmeResult> findResultById(FilmeId id);
}
```

### Comportamento de ListarFilmesUseCaseImpl
1. Monta cache key: `"filmes:listagem:" + (genero != null ? genero : "todos")`
2. Tenta `CachePort.get(cacheKey)`
3. Se ausente: chama `FilmeQueryPort.findAllAtivos(genero)` — **não usa `FilmeRepository`**
4. Armazena resultado no cache com TTL de 15 minutos
5. Retorna `List<FilmeResult>`

### Comportamento de CriarFilmeUseCaseImpl
1. Constrói `new Filme(command)` — invariantes validados no construtor
2. `FilmeRepository.save(filme)`
3. `CachePort.evictByPrefix("filmes:listagem:")`
4. `ApplicationEventPublisher.publishEvent(new FilmeCriadoEvent(salvo))`
5. Retorna `FilmeResult.from(salvo)`

---

## Infrastructure

### FilmeJpaEntity
- `@Entity @Table(name = "filme")`
- Campos espelham a tabela, não a entidade de domínio
- Sem lógica de negócio

### FilmeRepositoryAdapter
- `implements FilmeRepository` (domínio)
- Usa `FilmeJpaRepository` (Spring Data) internamente
- Mapper: `FilmeJpaMapper` converte `FilmeJpaEntity ↔ Filme`

### FilmeQueryAdapter
- `implements FilmeQueryPort` (aplicação)
- Query JPQL com `SELECT new FilmeResult(...)` — sem instanciar `Filme`
- Filtra por `ativo = true` e opcionalmente por `genero`

---

## Interface (HTTP)

### Endpoints
| Método | Path                  | Auth     | Body               | Response         |
|--------|-----------------------|----------|--------------------|------------------|
| GET    | `/api/v1/filmes`      | Pública  | —                  | `List<FilmeResponseDTO>` |
| GET    | `/api/v1/filmes/{id}` | Pública  | —                  | `FilmeResponseDTO` |
| POST   | `/api/v1/filmes`      | ADMIN    | `FilmeRequestDTO`  | `FilmeResponseDTO` (201) |
| PUT    | `/api/v1/filmes/{id}` | ADMIN    | `FilmeRequestDTO`  | `FilmeResponseDTO` |
| DELETE | `/api/v1/filmes/{id}` | ADMIN    | —                  | 204 No Content   |

### FilmeRequestDTO (validações)
```java
public record FilmeRequestDTO(
    @NotBlank String titulo,
    @NotNull Genero genero,
    @NotNull ClassificacaoEtaria classificacao,
    @Positive int duracaoMinutos,
    String posterUrl,
    @NotNull LocalDate dataLancamento
) {}
```

### FilmeHttpMapper
- `toCommand(FilmeRequestDTO) → CriarFilmeCommand`
- `toUpdateCommand(Long id, FilmeRequestDTO) → AtualizarFilmeCommand`
- `toResponse(FilmeResult) → FilmeResponseDTO`
- `toResponseList(List<FilmeResult>) → List<FilmeResponseDTO>`
