# AGENT.md — CineSystem · Orquestração do Codex CLI

## O que é este arquivo

Este arquivo é lido pelo Codex CLI em toda execução. Ele define
as regras globais, o mapa de contexto e a ordem de desenvolvimento.
Nunca remova ou renomeie este arquivo.

---

## Stack

| Camada       | Tecnologia                        |
|--------------|-----------------------------------|
| Linguagem    | Java 21                           |
| Framework    | Spring Boot 3.x                   |
| Persistência | Spring Data JPA + PostgreSQL 16   |
| Cache        | Redis 7 via Spring Data Redis     |
| Segurança    | Spring Security 6 + JWT (jjwt)    |
| Migrations   | Flyway                            |
| Build        | Maven 3.9                         |
| Testes       | JUnit 5 + Mockito + Testcontainers|

---

## Mapa de contexto

Antes de executar qualquer prompt, o Codex CLI carrega os arquivos
de contexto listados abaixo. Cada prompt da pasta /prompts declara
quais arquivos de docs ele precisa — não carregue todos de uma vez.

```
docs/
├── architecture/
│   ├── clean-architecture.md     # Regra de dependência, camadas, diagrama
│   ├── package-structure.md      # Árvore completa de pacotes
│   └── layer-rules.md            # O que cada camada pode e não pode fazer
├── features/
│   ├── filme.md                  # Domínio, use cases, endpoints e exemplos
│   ├── sessao.md
│   ├── assento.md
│   ├── ingresso.md               # Inclui Outbox Pattern e reserva Redis
│   ├── auth.md                   # JWT, cadastro, login
│   └── admin.md                  # CRUD administrativo
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

1. **Regra de dependência:** `domain` ← `application` ← `interfaces` / `infrastructure`
2. **Domínio puro:** nenhuma classe em `domain/` recebe anotação de framework
3. **Use cases:** toda implementação deve satisfazer uma interface (porta de entrada)
4. **Commands e Results:** sempre `record` Java — nunca classes mutáveis
5. **Leituras:** usar `QueryPort` dedicada (CQRS tático) — não carregar entidade de domínio para ler
6. **Eventos transacionais:** sempre via Outbox Pattern — nunca `ApplicationEventPublisher` direto em fluxos de compra
7. **Controllers:** injetam apenas interfaces de use cases — nunca repositórios ou services
8. **Testes unitários:** mockam portas (interfaces), nunca implementações concretas

---

## Ordem de desenvolvimento recomendada

Execute os prompts nesta sequência. Cada fase depende da anterior.

### Fase 1 — Fundação
```
prompts/scaffolding/01-project-structure.md
prompts/scaffolding/02-domain-shared.md
prompts/scaffolding/03-security-config.md
prompts/database/01-migrations-base.md
```

### Fase 2 — Módulo Filme (referência de padrão)
```
prompts/features/filme/01-domain.md
prompts/features/filme/02-application.md
prompts/features/filme/03-infrastructure.md
prompts/features/filme/04-interface.md
prompts/validation/validate-filme.md
```

### Fase 3 — Módulo Sala e Sessão
```
prompts/features/sessao/01-domain.md
prompts/features/sessao/02-application.md
prompts/features/sessao/03-infrastructure.md
prompts/features/sessao/04-interface.md
```

### Fase 4 — Módulo Ingresso (Outbox + Redis)
```
prompts/features/ingresso/01-domain.md
prompts/features/ingresso/02-application.md        # ComprarIngressoUseCase + Outbox
prompts/features/ingresso/03-infrastructure.md     # JPA + RedisReservaAdapter
prompts/features/ingresso/04-outbox-scheduler.md   # OutboxProcessorScheduler
prompts/features/ingresso/05-interface.md
prompts/database/02-migrations-outbox.md
```

### Fase 5 — Auth e Admin
```
prompts/features/auth/01-domain.md
prompts/features/auth/02-application.md
prompts/features/auth/03-infrastructure.md
prompts/features/auth/04-interface.md
prompts/features/admin/01-admin-controller.md
```

### Fase 6 — Validação e Testes
```
prompts/validation/validate-dependency-rule.md
prompts/validation/generate-unit-tests.md
prompts/validation/generate-integration-tests.md
```

---

## Como executar um prompt

```bash
# Sintaxe padrão
codex --context <arquivo-de-doc> [--context <outro-doc>] < prompts/<caminho>.md

# Exemplo real — implementar domínio do filme
codex \
  --context docs/architecture/clean-architecture.md \
  --context docs/architecture/layer-rules.md \
  --context docs/features/filme.md \
  < prompts/features/filme/01-domain.md

# Usando o script wrapper (recomendado)
./scripts/codex-run.sh prompts/features/filme/01-domain.md
```

---

## Script wrapper: scripts/codex-run.sh

O script lê o cabeçalho `context:` de cada prompt e injeta
os arquivos de doc automaticamente. Veja `/scripts/codex-run.sh`.
