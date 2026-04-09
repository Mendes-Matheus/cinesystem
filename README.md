# CineSystem — Guia de Desenvolvimento com Codex CLI

Estrutura de documentação e prompts para desenvolvimento do backend
do CineSystem usando **Codex CLI** como agente de vibe coding.

---

## Pré-requisitos

```bash
# Instalar Codex CLI
npm install -g @openai/codex

# Verificar instalação
codex --version

# Tornar o script executável (uma vez)
chmod +x scripts/codex-run.sh
```

---

## Estrutura do repositório

```
.
├── AGENT.md                        # Regras globais — lido pelo Codex em toda sessão
├── README.md                       # Este arquivo
├── scripts/
│   └── codex-run.sh                # Wrapper que orquestra prompts e contexto
├── docs/
│   ├── architecture/
│   │   ├── clean-architecture.md   # Os 4 anéis, regra de dependência, trade-offs
│   │   ├── package-structure.md    # Árvore completa de pacotes Java
│   │   └── layer-rules.md          # O que cada camada pode e não pode fazer
│   ├── features/
│   │   ├── filme.md                # Domínio, use cases, endpoints, cache
│   │   ├── sessao.md               # Sessões e assentos
│   │   ├── ingresso.md             # Compra, Outbox Pattern, reserva Redis
│   │   ├── auth.md                 # JWT, login, cadastro, blacklist
│   │   └── admin.md                # Painel administrativo
│   ├── database/
│   │   ├── erd.md                  # ERD em Mermaid
│   │   ├── tables.md               # DDL completo com migrations
│   │   └── indexes.md              # Índices com justificativas
│   └── conventions/
│       ├── naming.md               # Nomenclatura de classes, métodos, banco
│       ├── patterns.md             # Outbox, CQRS, Strategy, records
│       ├── testing.md              # Padrões de teste por camada
│       └── error-handling.md       # Hierarquia de exceções, mapeamento HTTP
└── prompts/
    ├── scaffolding/                # Estrutura inicial
    │   ├── 01-project-structure.md
    │   ├── 02-domain-shared.md
    │   └── 03-security-config.md
    ├── features/
    │   ├── filme/     01→04        # domain → application → infra → interface
    │   ├── sessao/    01→04
    │   ├── ingresso/  01→05        # inclui outbox-scheduler
    │   ├── auth/      01→04
    │   └── admin/     01           # AdminController
    ├── database/
    │   ├── 01-migrations-base.md
    │   └── 02-migrations-outbox.md
    └── validation/
        ├── validate-filme.md
        ├── validate-dependency-rule.md
        ├── generate-unit-tests.md
        └── generate-integration-tests.md
```

---

## Como funciona o wrapper

Cada prompt declara no cabeçalho YAML quais docs ele precisa:

```markdown
---
context:
  - docs/architecture/layer-rules.md
  - docs/features/filme.md
---

# Tarefa: Implementar domain/filme
...
```

O script `codex-run.sh` lê esse cabeçalho e injeta apenas os arquivos
necessários como `--context` para o Codex. O `AGENT.md` é sempre incluído.

```bash
# O que o script executa internamente:
codex \
  --context AGENT.md \
  --context docs/architecture/layer-rules.md \
  --context docs/features/filme.md \
  < prompts/features/filme/01-domain.md
```

---

## Fluxo de desenvolvimento recomendado

### Fase 1 — Fundação (~1 sessão)

```bash
./scripts/codex-run.sh --chain scaffolding
```

Cria: estrutura de pacotes, domain/shared, configurações Spring, migrations V1–V4.

### Fase 2 — Módulo Filme (~1 sessão)

O módulo Filme é o **módulo de referência**. Implemente-o primeiro e
valide antes de avançar — os demais módulos seguem o mesmo padrão.

```bash
./scripts/codex-run.sh --chain fase2
```

Cria: Filme (domain → application → infra → interface) + testes unitários.

### Fase 3 — Sessão e Assento (~1 sessão)

```bash
./scripts/codex-run.sh --chain fase3
```

### Fase 4 — Ingresso (a mais complexa) (~2 sessões)

```bash
./scripts/codex-run.sh --chain fase4
```

Cria: domínio do Ingresso, ComprarIngressoUseCase com Outbox Pattern,
RedisReservaAdapter, OutboxProcessorScheduler, seed de dados.

### Fase 5 — Auth (~1 sessão)

```bash
./scripts/codex-run.sh --chain fase5
```

### Fase 6 — Admin (~1 sessão)

```bash
./scripts/codex-run.sh --chain admin
```

Cria: `DesativarUsuarioUseCase`, `RelatorioSessaoUseCase`, `ListarUsuariosUseCase`,
`UsuarioQueryPort`, `UsuarioQueryAdapter` e `AdminController`.

### Fase 7 — Validação e testes (~1 sessão)

```bash
./scripts/codex-run.sh --chain validacao
```

Audita a regra de dependência e gera testes unitários + integração.

---

## Comandos úteis

```bash
# Ver o que um prompt vai fazer sem executar (recomendado antes de rodar)
./scripts/codex-run.sh --dry-run prompts/features/ingresso/02-application.md

# Listar todos os prompts disponíveis com título
./scripts/codex-run.sh --list

# Executar um prompt individual
./scripts/codex-run.sh prompts/features/filme/01-domain.md

# Executar uma chain completa
./scripts/codex-run.sh --chain fase2
```

---

## Ordem dos prompts dentro de cada feature

Sempre respeite a sequência `01 → 02 → 03 → 04`:

```
01-domain.md         # Entidades, Value Objects, interfaces de repositório
02-application.md    # Use cases, Commands, Results, QueryPorts
03-infrastructure.md # JpaEntity, adaptadores, Redis
04-interface.md      # Controller, DTOs HTTP, mappers
```

Cada prompt assume que os anteriores já foram executados.
Executar fora de ordem causa imports ausentes.

---

## Dicas de uso com Codex CLI

### Antes de cada sessão

1. Rode `--dry-run` para conferir o contexto que será carregado
2. Verifique se o projeto compila (`mvn compile -q`) antes de começar
3. Faça commit do estado atual para ter um ponto de rollback

### Se o Codex gerar código incorreto

O problema mais comum é violação da regra de dependência.
Execute o prompt de auditoria:

```bash
./scripts/codex-run.sh prompts/validation/validate-dependency-rule.md
```

### Adicionando novos prompts

1. Crie o arquivo em `prompts/{categoria}/{numero}-{nome}.md`
2. Adicione o cabeçalho YAML com `context:` apontando para os docs relevantes
3. Use a seção `## Checklist` no final para guiar a validação automática
4. Se for uma fase nova, adicione ao array `chains` em `scripts/codex-run.sh`

---

## Variáveis de ambiente necessárias

Crie um arquivo `.env` na raiz (não commitar):

```bash
DB_URL=jdbc:postgresql://localhost:5432/cinesystem
DB_USER=cineuser
DB_PASS=cinepass
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=sua-chave-secreta-com-pelo-menos-256-bits-de-entropia
```

Para rodar com Docker:

```bash
docker-compose up -d
```

---

## Arquitetura resumida

```
┌──────────────────────────────────────────────────────┐
│  interfaces/   Controllers · Schedulers · Handlers   │
│  ┌────────────────────────────────────────────────┐  │
│  │  infrastructure/   JPA · Redis · JWT · Mail    │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │  application/   Use Cases · Ports · DTOs  │  │  │
│  │  │  ┌────────────────────────────────────┐  │  │  │
│  │  │  │  domain/   Entities · VOs · Repos  │  │  │  │
│  │  │  └────────────────────────────────────┘  │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘

Regra: dependências apontam para dentro. domain/ não conhece nada externo.
```
