# Convenções de Tratamento de Erros

## Hierarquia de exceções

```
RuntimeException
└── DomainException                  (domain/shared/)
    └── ResourceNotFoundException    (domain/shared/)
```

Toda exceção de negócio herda de `DomainException`.
Nunca crie exceções checked no domínio.

---

## Mapeamento Exceção → HTTP

| Exceção | Status | Código JSON | Quando usar |
|---------|--------|-------------|-------------|
| `DomainException` | 422 Unprocessable Entity | `DOMAIN_ERROR` | Violação de regra de negócio |
| `ResourceNotFoundException` | 404 Not Found | `NOT_FOUND` | Entidade não encontrada por ID |
| `MethodArgumentNotValidException` | 400 Bad Request | `VALIDATION_ERROR` | Falha em `@Valid` de request DTO |
| `AccessDeniedException` | 403 Forbidden | `FORBIDDEN` | Usuário sem permissão |
| `Exception` (fallback) | 500 Internal Server Error | `INTERNAL_ERROR` | Erros não previstos |

---

## Formato padrão de resposta de erro

```json
{
  "codigo": "DOMAIN_ERROR",
  "mensagem": "Assento indisponível ou já reservado",
  "timestamp": "2026-03-15T14:32:00",
  "detalhes": []
}
```

Para erros de validação (400), `detalhes` é populado:
```json
{
  "codigo": "VALIDATION_ERROR",
  "mensagem": "Dados inválidos",
  "timestamp": "2026-03-15T14:32:00",
  "detalhes": [
    "titulo: não deve estar em branco",
    "duracaoMinutos: deve ser maior que 0"
  ]
}
```

---

## Regras por camada

### domain/
- Lança `DomainException` para violações de invariante
- Lança `ResourceNotFoundException` para entidade inexistente (use cases)
- **Nunca** captura exceção — apenas lança
- Mensagens em português, descritivas e sem stack trace

### application/
- Propaga `DomainException` sem capturar
- Use cases lançam `ResourceNotFoundException` quando `Optional.empty()` for resultado de busca:
  ```java
  filmeRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado: " + id.valor()));
  ```
- **Nunca** usa `try/catch` para controle de fluxo de negócio

### infrastructure/
- Captura exceções técnicas (ex: `DataIntegrityViolationException`) e converte em `DomainException`:
  ```java
  try {
      return jpaRepository.save(entity);
  } catch (DataIntegrityViolationException ex) {
      throw new DomainException("E-mail já cadastrado");
  }
  ```

### interfaces/
- `GlobalExceptionHandler` é o único lugar que converte exceções em respostas HTTP
- Controllers **nunca** têm try/catch — deixam exceções propagar
- Stack trace **nunca** é exposto na resposta ao cliente
- Fallback `Exception` loga com `log.error("Erro não tratado", ex)` antes de retornar 500

### OutboxProcessorScheduler (caso especial)
- **Absorve** exceções de processamento — nunca deixa o scheduler quebrar
- Registra falha no próprio `OutboxEvent` (`evento.registrarFalha(ex.getMessage())`)
- Loga com `log.warn` para alertas de monitoramento

---

## Mensagens de segurança

Nunca revelar informações sensíveis nas mensagens de erro:

```java
// ERRADO — revela que o e-mail existe no sistema
throw new DomainException("Senha incorreta para o e-mail " + email);

// CORRETO — mensagem genérica
throw new DomainException("Credenciais inválidas");
```

```java
// ERRADO — expõe detalhe de infra
throw new DomainException("Falha ao conectar em redis://localhost:6379");

// CORRETO — mensagem de domínio
throw new DomainException("Serviço temporariamente indisponível");
```
