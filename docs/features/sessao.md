# Feature: Sessão

## Domínio

### Entidade: Sessao
```
id              SessaoId
filmeId         FilmeId
salaId          SalaId
dataHora        LocalDateTime    NOT NULL, deve ser futura
idioma          String           DUBLADO | LEGENDADO | ORIGINAL
formato         FormatoExibicao  2D | 3D | IMAX
preco           BigDecimal       > 0
status          StatusSessao     ATIVA | LOTADA | CANCELADA | ENCERRADA
```

### Entidade: SessaoAssento
```
id              Long
sessaoId        SessaoId
assentoId       AssentoId
status          StatusAssento    DISPONIVEL | RESERVADO | OCUPADO
reservadoAte    LocalDateTime    nullable (TTL da reserva temporária)
usuarioId       UsuarioId        nullable
```

### Invariantes de domínio
- `dataHora` deve ser no futuro → `DomainException("Sessão deve ser agendada no futuro")`
- `preco` deve ser positivo → `DomainException("Preço deve ser maior que zero")`
- `cancelar()` só permitido se status == ATIVA

### SessaoAssento.confirmarCompra(UsuarioId)
- Valida que status == DISPONIVEL ou RESERVADO pelo mesmo usuário
- Muda status para OCUPADO
- Cria e retorna `Ingresso` com `CodigoIngresso.gerar()`

### Interface de repositório
```java
public interface SessaoRepository {
    Sessao save(Sessao sessao);
    Optional<Sessao> findById(SessaoId id);
    Optional<SessaoAssento> findSessaoAssento(SessaoId sessaoId, AssentoId assentoId);
    List<SessaoAssento> findAssentosDisponiveis(SessaoId sessaoId);
}
```

---

## Application

### Use Cases
| Interface | Porta de entrada |
|-----------|-----------------|
| `ListarSessoesPorFilmeUseCase` | `execute(FilmeId): List<SessaoResult>` |
| `BuscarAssentosUseCase` | `execute(SessaoId): List<AssentoResult>` |
| `CriarSessaoUseCase` | `execute(CriarSessaoCommand): SessaoResult` |
| `CancelarSessaoUseCase` | `execute(SessaoId): void` |

### Commands
```java
public record CriarSessaoCommand(
    FilmeId filmeId, SalaId salaId, LocalDateTime dataHora,
    String idioma, FormatoExibicao formato, BigDecimal preco
) {}
```

### Results
```java
public record SessaoResult(
    Long id, Long filmeId, String tituloFilme, Long salaId, String nomeSala,
    LocalDateTime dataHora, String idioma, String formato,
    BigDecimal preco, String status, int assentosDisponiveis
) {}

public record AssentoResult(
    Long id, String fileira, int numero, String tipo, String status
) {}
```

### Porta de leitura
```java
public interface SessaoQueryPort {
    List<SessaoResult> findAtivasByFilme(FilmeId filmeId);
    List<AssentoResult> findAssentosBySessao(SessaoId sessaoId);
}
```

### Cache
- Chave: `"sessoes:filme:{filmeId}"` — TTL: 5 minutos
- Chave: `"assentos:sessao:{sessaoId}"` — TTL: 30 segundos (dado volátil)
- Evict ao criar ou cancelar sessão

---

## Interface (HTTP)

| Método | Path | Auth | Response |
|--------|------|------|----------|
| GET | `/api/v1/filmes/{id}/sessoes` | Pública | `List<SessaoResponseDTO>` |
| GET | `/api/v1/sessoes/{id}/assentos` | Autenticado | `List<AssentoResponseDTO>` |
| POST | `/api/v1/sessoes` | ADMIN | `SessaoResponseDTO` (201) |
| DELETE | `/api/v1/sessoes/{id}` | ADMIN | 204 |
