---
context:
  - docs/architecture/layer-rules.md
  - docs/features/ingresso.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar domain/ingresso e domain/pagamento

Os arquivos de `domain/shared/` já existem (`DomainException`, `AggregateRoot`, `DomainEvent`).

---

## domain/ingresso/

### IngressoId.java

- `record IngressoId(Long valor)` — compact constructor rejeita null

### CodigoIngresso.java

- `record CodigoIngresso(String valor)`
- Compact constructor: rejeita null e blank com `DomainException("Código de ingresso inválido")`
- Método estático de fábrica:
  
  ```java
  public static CodigoIngresso gerar() {
      return new CodigoIngresso(UUID.randomUUID().toString());
  }
  ```

### StatusIngresso.java

- `enum`: `ATIVO, UTILIZADO, CANCELADO, EXPIRADO`

### Ingresso.java

- POJO puro — zero imports de framework
- Campos:
  
  ```
  IngressoId       id
  CodigoIngresso   codigo
  UsuarioId        usuarioId
  Long             sessaoAssentoId   (referência simples, não objeto)
  BigDecimal       valorPago
  StatusIngresso   status
  LocalDateTime    compradoEm
  ```
- Construtor público:
  - `codigo` gerado via `CodigoIngresso.gerar()` se não fornecido
  - `valorPago` deve ser > 0 → `DomainException("Valor do ingresso deve ser positivo")`
  - `status` iniciado como `StatusIngresso.ATIVO`
  - `compradoEm` iniciado como `LocalDateTime.now()`
- Método `cancelar()`:
  - Lança `DomainException("Ingresso não pode ser cancelado: status " + status)` se status != ATIVO
  - Muda status para CANCELADO
- Método `marcarUtilizado()`:
  - Lança `DomainException("Ingresso já foi utilizado ou cancelado")` se status != ATIVO
  - Muda status para UTILIZADO
- Getters para todos os campos (sem setters públicos)
- `equals()` e `hashCode()` baseados em `id`

### IngressoRepository.java

```java
public interface IngressoRepository {
    Ingresso save(Ingresso ingresso);
    Optional<Ingresso> findById(IngressoId id);
    Optional<Ingresso> findByCodigo(CodigoIngresso codigo);
}
```

---

## domain/pagamento/

### MetodoPagamento.java

- `enum`: `CARTAO_CREDITO, CARTAO_DEBITO, PIX, BOLETO`

### StatusPagamento.java

- `enum`: `PENDENTE, APROVADO, RECUSADO, ESTORNADO`

### Pagamento.java

- POJO puro
- Campos:
  
  ```
  Long         id
  Long         ingressoId
  String       transacaoId    (nullable — gerado pelo gateway externo)
  BigDecimal   valor
  MetodoPagamento metodo
  StatusPagamento status
  LocalDateTime   processadoEm   (nullable)
  ```
- Construtor inicializa `status = PENDENTE`
- Método `aprovar(String transacaoId)`:
  - Valida status == PENDENTE → `DomainException`
  - Seta `transacaoId`, `status = APROVADO`, `processadoEm = now()`
- Método `estornar()`:
  - Valida status == APROVADO → `DomainException`
  - Seta `status = ESTORNADO`

### PagamentoRepository.java

```java
public interface PagamentoRepository {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findByIngressoId(Long ingressoId);
}
```

---

## Checklist

- [ ] `CodigoIngresso.gerar()` usa `UUID.randomUUID()` — não hash, não sequencial
- [ ] `Ingresso.cancelar()` inclui o status atual na mensagem de erro
- [ ] Nenhum arquivo importa `org.springframework.*` ou `jakarta.*`
- [ ] `Pagamento.aprovar()` seta `processadoEm` com `LocalDateTime.now()`
- [ ] `IngressoRepository` e `PagamentoRepository` são interfaces sem anotações
