# Regras por Camada

## domain/

### Permitido
- Classes Java puras (POJO)
- Construtores que validam invariantes e lançam `DomainException`
- Enums de domínio (`Genero`, `Role`, `StatusIngresso`)
- Value Objects como `record` com validação no compact constructor
- Interfaces de repositório (apenas assinatura — sem implementação)
- `DomainException extends RuntimeException`

### Proibido
- Qualquer import de `org.springframework.*`
- Qualquer import de `jakarta.persistence.*`
- Qualquer import de bibliotecas externas (Jackson, Lombok, etc.)

### Exemplo correto
```java
// domain/usuario/Email.java
public record Email(String valor) {
    public Email {
        if (valor == null || !valor.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$"))
            throw new DomainException("E-mail inválido: " + valor);
    }
}
```

---

## application/

### Permitido
- `@Service`, `@Transactional` (trade-off consciente — ver clean-architecture.md)
- `@RequiredArgsConstructor` (Lombok)
- Injetar interfaces: `FilmeRepository`, `CachePort`, `FilmeQueryPort`, `OutboxRepository`
- `ApplicationEventPublisher` apenas para eventos não-transacionais
- `record` para Commands e Results

### Proibido
- Injetar `RedisTemplate`, `JpaRepository` ou qualquer classe de infraestrutura
- `@Entity`, `@Column`, `@Query`
- Lógica de serialização/deserialização HTTP
- Carregar entidade de domínio apenas para leitura — usar `QueryPort`

### Padrão CQRS tático (obrigatório para listagens)
```
Leitura  → QueryPort  → projeção SQL direta → Result (sem entidade de domínio)
Escrita  → Repository → Entidade de domínio → save()
```

### Padrão Outbox (obrigatório para eventos transacionais)
```java
// CORRETO — evento salvo na mesma @Transactional do ingresso
outboxRepository.save(OutboxEvent.of("IngressoComprado", payload));

// ERRADO — ApplicationEventPublisher fora da transação pode perder o evento
publisher.publishEvent(new IngressoCompradoEvent(salvo));
```

---

## infrastructure/

### Permitido
- `@Repository`, `@Component`, `@Entity`, `@Table`, `@Column`
- `JpaRepository`, `RedisTemplate`, `JavaMailSender`
- Implementar interfaces de `domain/` e `application/port/out/`
- Mappers entre `JpaEntity` ↔ `DomainEntity`

### Proibido
- Conter lógica de negócio
- Injetar use cases da camada `application/`
- Referências a classes de `interfaces/`

### Regra dos adaptadores
Cada adaptador implementa **exatamente uma** interface de porta.
`FilmeRepositoryAdapter` implements `FilmeRepository` — nada mais.
`FilmeQueryAdapter` implements `FilmeQueryPort` — nada mais.

---

## interfaces/

### Permitido
- `@RestController`, `@RequestMapping`, `@PreAuthorize`
- `@RestControllerAdvice` para tratamento de exceções
- `@Scheduled` para schedulers
- Injetar **somente interfaces** de use cases

### Proibido
- Injetar `JpaRepository`, `RedisTemplate` ou qualquer classe de infraestrutura
- Injetar `FilmeRepositoryAdapter` ou qualquer implementação concreta
- Conter lógica de negócio ou transformação além de mapeamento HTTP ↔ Command/Result

### Mapeamento de exceções → HTTP
| Exceção                          | Status HTTP |
|----------------------------------|-------------|
| `DomainException`                | 422         |
| `ResourceNotFoundException`      | 404         |
| `MethodArgumentNotValidException`| 400         |
| `AccessDeniedException`          | 403         |
| `Exception` (fallback)           | 500         |
