---
context:
  - docs/architecture/clean-architecture.md
  - docs/architecture/layer-rules.md
---

# Tarefa: Implementar domain/shared

Implemente os três arquivos da camada `domain/shared/`.
Nenhum deles pode importar qualquer classe de framework externo.

## DomainException.java

```
pacote: com.cinesystem.domain.shared
extends: RuntimeException
construtores: (String message) e (String message, Throwable cause)
```

## AggregateRoot.java

```
pacote: com.cinesystem.domain.shared
classe abstrata
campo: List<DomainEvent> domainEvents = new ArrayList<>()
método: protected void registerEvent(DomainEvent event)
método: public List<DomainEvent> pullDomainEvents()
         — retorna cópia imutável e limpa a lista interna
```

## DomainEvent.java

```
pacote: com.cinesystem.domain.shared
interface
método: LocalDateTime occurredOn()
método: default String eventType() { return this.getClass().getSimpleName(); }
```

## Validação

Após gerar os três arquivos, confirme que:
- Nenhum arquivo tem import de `org.springframework.*` ou `jakarta.*`
- `AggregateRoot.pullDomainEvents()` retorna `List.copyOf()` e chama `clear()`
