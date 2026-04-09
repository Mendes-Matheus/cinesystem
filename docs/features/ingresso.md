# Feature: Ingresso

## Domínio

### Entidade: Ingresso
```
id              IngressoId
codigo          CodigoIngresso   Value Object (UUID)
usuarioId       UsuarioId
sessaoAssentoId (referência ao SessaoAssento)
valorPago       BigDecimal       > 0
status          StatusIngresso   ATIVO | UTILIZADO | CANCELADO | EXPIRADO
compradoEm      LocalDateTime
```

### Value Object: CodigoIngresso
```java
public record CodigoIngresso(String valor) {
    public CodigoIngresso {
        if (valor == null || valor.isBlank())
            throw new DomainException("Código de ingresso inválido");
    }
    public static CodigoIngresso gerar() {
        return new CodigoIngresso(UUID.randomUUID().toString());
    }
}
```

### Invariantes de domínio
- `cancelar()` lança `DomainException` se status != ATIVO
- `marcarUtilizado()` lança `DomainException` se status != ATIVO

### Interface de repositório
```java
public interface IngressoRepository {
    Ingresso save(Ingresso ingresso);
    Optional<Ingresso> findById(IngressoId id);
    Optional<Ingresso> findByCodigo(CodigoIngresso codigo);
}
```

---

## Application

### Use Cases
| Interface | Porta de entrada |
|-----------|-----------------|
| `ComprarIngressoUseCase` | `execute(ComprarIngressoCommand): IngressoResult` |
| `CancelarIngressoUseCase` | `execute(CancelarIngressoCommand): IngressoResult` |

### Commands
```java
public record ComprarIngressoCommand(
    SessaoId sessaoId,
    AssentoId assentoId,
    UsuarioId usuarioId,
    MetodoPagamento metodoPagamento
) {}

public record CancelarIngressoCommand(IngressoId ingressoId, UsuarioId usuarioId) {}
```

### Result
```java
public record IngressoResult(
    Long id, String codigo, Long sessaoId, Long assentoId,
    String fileira, int numeroAssento, String tituloFilme,
    LocalDateTime dataHora, BigDecimal valorPago, String status
) {}
```

### Porta de leitura
```java
public interface IngressoQueryPort {
    List<IngressoResult> findByUsuario(UsuarioId usuarioId);
    Optional<IngressoResult> findResultById(IngressoId id);
}
```

---

## Outbox Pattern — OBRIGATÓRIO nesta feature

O evento `IngressoComprado` DEVE ser salvo na mesma `@Transactional`
do `Ingresso`. Nunca usar `ApplicationEventPublisher` direto aqui.

### OutboxEvent
```java
public class OutboxEvent {
    private Long id;
    private String eventType;       // "IngressoComprado"
    private String aggregateId;     // ingressoId.toString()
    private String payload;         // JSON serializado
    private String status;          // PENDENTE | PROCESSADO | FALHA
    private int tentativas;
    private LocalDateTime criadoEm;
    private LocalDateTime processadoEm;

    public static OutboxEvent of(String type, String aggregateId, Object payload) { ... }
    public void marcarProcessado() { this.status = "PROCESSADO"; this.processadoEm = now(); }
    public void registrarFalha(String motivo) { this.tentativas++; this.status = "FALHA"; }
}
```

### IngressoCompradoPayload
```java
public record IngressoCompradoPayload(
    Long ingressoId, String codigo, String emailUsuario,
    String tituloFilme, LocalDateTime dataHora,
    String fileira, int numeroAssento, BigDecimal valorPago
) {}
```

### OutboxRepository
```java
public interface OutboxRepository {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findPendentes(int limit);
}
```

### ComprarIngressoUseCaseImpl — fluxo completo
```
1. sessaoRepository.findSessaoAssento(sessaoId, assentoId)  → lança NOT_FOUND se ausente
2. reservaPort.reservar(sessaoId, assentoId, usuarioId)      → retorna false = lança DomainException
3. sessaoAssento.confirmarCompra(usuarioId)                  → gera Ingresso com CodigoIngresso.gerar()
4. ingressoRepository.save(ingresso)
5. outboxRepository.save(OutboxEvent.of("IngressoComprado", id, payload))  ← MESMA @Transactional
6. return IngressoResult.from(salvo)
```

---

## Infrastructure

### ReservaAssentoPort (Redis — SETNX atômico)
```java
public interface ReservaAssentoPort {
    boolean reservar(SessaoId sessaoId, AssentoId assentoId, UsuarioId usuarioId);
    void liberar(SessaoId sessaoId, AssentoId assentoId);
}
// Chave Redis: "reserva:{sessaoId}:{assentoId}" com TTL de 10 minutos
// Usar redisTemplate.opsForValue().setIfAbsent() para atomicidade
```

### OutboxRepositoryAdapter
- `implements OutboxRepository`
- `findPendentes(limit)`: busca eventos com `status = 'PENDENTE'` ordenado por `criadoEm ASC`

---

## Interface (HTTP)

### Endpoints
| Método | Path                        | Auth          | Body                    | Response |
|--------|-----------------------------|---------------|-------------------------|----------|
| POST   | `/api/v1/ingressos`         | Autenticado   | `ComprarIngressoRequestDTO` | `IngressoResponseDTO` (201) |
| DELETE | `/api/v1/ingressos/{id}`    | Autenticado   | —                       | 204      |
| GET    | `/api/v1/ingressos/meus`    | Autenticado   | —                       | `List<IngressoResponseDTO>` |
| GET    | `/api/v1/ingressos/{id}`    | Autenticado   | —                       | `IngressoResponseDTO` |

---

## Scheduler: OutboxProcessorScheduler
- `@Scheduled(fixedDelay = 5_000)`
- Busca até 50 eventos `PENDENTE` por ciclo
- Para cada evento: `despachar(evento)` → `marcarProcessado()` ou `registrarFalha()`
- `despachar` roteia por `eventType`:
  - `"IngressoComprado"` → `EmailPort.enviarConfirmacao()` + `QRCodePort.gerarEEnviar()`
