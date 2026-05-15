# AGENT.md — CineSystem

## O que é este arquivo

Este arquivo deve ser lido pelo agente de IA no início de toda execução.
Ele define as regras globais, o mapa de contexto e a ordem de desenvolvimento.
É compatível com qualquer agente que suporte injeção de contexto via arquivos
(Codex, Claude Code, Gemini, Aider, Cursor, etc.).

**Nunca remova ou renomeie este arquivo.**

---

## Stack

| Camada       | Tecnologia                       |
|--------------|----------------------------------|
| Linguagem    | Java 21                          |
| Framework    | Spring Boot 4.x                  |
| Persistência | Spring Data JPA + PostgreSQL     |
| Cache        | Redis 7                          |
| Segurança    | Spring Security + JWT (jjwt)     |
| Migrations   | Flyway                           |
| Build        | Maven 3.9                        |
| Testes       | JUnit + Mockito + Testcontainers |

---

## Mapa de contexto

Cada prompt em `spec-kit/prompts` declara quais arquivos de `spec-kit/docs/` ele precisa.
**Carregue apenas os arquivos declarados pelo prompt em execução** — não injete
todos de uma vez.

```
spec-kit/docs/
├── architecture/
│   ├── clean-architecture.md     # Regra de dependência, camadas, diagrama
│   ├── package-structure.md      # Árvore completa de pacotes
│   └── layer-rules.md            # O que cada camada pode e não pode fazer
├── features/
│   ├── filme.md                  # Domínio, use cases, endpoints e exemplos
│   ├── sessao.md                 # Sessões e assentos
│   ├── ingresso.md               # Inclui Outbox Pattern e reserva Redis
│   ├── auth.md                   # JWT, cadastro, login
│   ├── pagamento.md              # Pagamento com Mercado Pago
│   └── admin.md                  # Painel administrativo
├── database/
│   ├── erd.md                    # ERD completo em Mermaid
│   ├── tables.md                 # DDL de todas as tabelas com tipos e constraints
│   └── indexes.md                # Índices recomendados com justificativa
└── conventions/
    ├── naming.md                 # Nomenclatura de classes, métodos, pacotes
    ├── error-handling.md         # DomainException, GlobalExceptionHandler, códigos HTTP
    ├── testing.md                # Padrões de teste por camada
    └── patterns.md               # Outbox, CQRS tático, Strategy, records
```

---

## Regras globais (sempre aplicadas)

Estas regras têm prioridade sobre qualquer instrução do prompt em execução.

1. **Regra de dependência:** `domain` ← `application` ← `interfaces` / `infrastructure`
2. **Domínio puro:** nenhuma classe em `domain/` recebe anotação de framework
3. **Use cases:** toda implementação deve satisfazer uma interface (porta de entrada)
4. **Commands e Results:** sempre `record` Java — nunca classes mutáveis
5. **Leituras:** usar `QueryPort` dedicada (CQRS tático) — não carregar entidade de domínio para ler
6. **Eventos transacionais:** sempre via Outbox Pattern — nunca `ApplicationEventPublisher` direto em fluxos de compra
7. **Controllers:** injetam apenas interfaces de use cases — nunca repositórios ou services diretamente
8. **Testes unitários:** mockam portas (interfaces), nunca implementações concretas

---

## Ordem de desenvolvimento recomendada

Execute os prompts nesta sequência. Cada fase depende da anterior.

### Fase 1 — Fundação
```
spec-kit/prompts/scaffolding/01-project-structure.md
spec-kit/prompts/scaffolding/02-domain-shared.md
spec-kit/prompts/scaffolding/03-security-config.md
spec-kit/prompts/database/01-migrations-base.md
```

### Fase 2 — Módulo Filme (referência de padrão)
```
spec-kit/prompts/features/filme/01-domain.md
spec-kit/prompts/features/filme/02-application.md
spec-kit/prompts/features/filme/03-infrastructure.md
spec-kit/prompts/features/filme/04-interface.md
spec-kit/prompts/validation/validate-filme.md
```

### Fase 3 — Módulo Sala e Sessão
```
spec-kit/prompts/features/sessao/01-domain.md
spec-kit/prompts/features/sessao/02-application.md
spec-kit/prompts/features/sessao/03-infrastructure.md
spec-kit/prompts/features/sessao/04-interface.md
```

### Fase 4 — Módulo Ingresso (Outbox + Redis)
```
spec-kit/prompts/features/ingresso/01-domain.md
spec-kit/prompts/features/ingresso/02-application.md        # ComprarIngressoUseCase + Outbox
spec-kit/prompts/features/ingresso/03-infrastructure.md     # JPA + RedisReservaAdapter
spec-kit/prompts/features/ingresso/04-outbox-scheduler.md   # OutboxProcessorScheduler
spec-kit/prompts/features/ingresso/05-interface.md
spec-kit/prompts/database/02-migrations-outbox.md
```

### Fase 5 — Auth
```
spec-kit/prompts/features/auth/01-domain.md
spec-kit/prompts/features/auth/02-application.md
spec-kit/prompts/features/auth/03-infrastructure.md
spec-kit/prompts/features/auth/04-interface.md
```

### Fase 6 — Admin
```
spec-kit/prompts/features/admin/01-admin-controller.md
```

### Fase 7 — Pagamento
```
spec-kit/prompts/features/pagamento/01-domain.md
spec-kit/prompts/features/pagamento/02-application.md
spec-kit/prompts/features/pagamento/03-infrastructure.md
spec-kit/prompts/features/pagamento/04-webhook.md
spec-kit/prompts/features/pagamento/05-interface.md
```

### Fase 8 — Validação e Testes
```
spec-kit/prompts/validation/validate-dependency-rule.md
spec-kit/prompts/validation/generate-unit-tests.md
spec-kit/prompts/validation/generate-integration-tests.md
```

---

## Como executar um prompt

Cada prompt é um arquivo Markdown em `/prompts`. Seu cabeçalho declara
os arquivos de contexto necessários. O agente deve carregá-los antes de
processar as instruções do prompt.

### Formato do cabeçalho de prompt

```
context:
  - spec-kit/docs/architecture/clean-architecture.md
  - spec-kit/docs/architecture/layer-rules.md
  - spec-kit/docs/features/filme.md
```

### Execução via script wrapper (recomendado)

O script `spec-kit/scripts/agent-run.sh` lê o cabeçalho `context:` automaticamente
e injeta os arquivos de doc no agente configurado:

```bash
.spec-kit/scripts/agent-run.sh spec-kit/spec-kit/prompts/features/filme/01-domain.md
```

### Execução manual por agente

Se preferir invocar o agente diretamente, injete os contextos declarados
no cabeçalho do prompt. Exemplos:

```bash
# Codex CLI
codex \
  --context spec-kit/docs/architecture/clean-architecture.md \
  --context spec-kit/docs/architecture/layer-rules.md \
  --context spec-kit/docs/features/filme.md \
  < spec-kit/spec-kit/prompts/features/filme/01-domain.md

# Gemini CLI
gemini \
  --file spec-kit/docs/architecture/clean-architecture.md \
  --file spec-kit/docs/architecture/layer-rules.md \
  --file spec-kit/docs/features/filme.md \
  < spec-kit/spec-kit/prompts/features/filme/01-domain.md

# Aider
aider \
  --read spec-kit/docs/architecture/clean-architecture.md \
  --read spec-kit/docs/architecture/layer-rules.md \
  --read spec-kit/docs/features/filme.md \
  --message "$(cat spec-kit/spec-kit/prompts/features/filme/01-domain.md)"
```

> Para outros agentes, consulte sua documentação sobre como injetar
> arquivos de contexto antes de processar uma instrução.

---

## Script wrapper: spec-kit/scripts/agent-run.sh

Consulte `spec-kit/scripts/agent-run.sh` para ver como o agente-alvo é configurado
e como os contextos são injetados automaticamente a partir do cabeçalho
de cada prompt.