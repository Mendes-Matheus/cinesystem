---
context:
  - docs/database/tables.md
---

# Tarefa: Criar migrations Flyway

Crie os 3 arquivos SQL de migração na pasta
`src/main/resources/db/migration/`.

Os scripts devem ser exatamente os definidos em `docs/database/tables.md`,
copiados sem alteração para os arquivos abaixo:

## V1__create_core_tables.sql
Conteúdo: seção "V1__create_core_tables.sql" da documentação.

## V2__create_outbox.sql
Conteúdo: seção "V2__create_outbox.sql" da documentação.

## V3__create_indexes.sql
Conteúdo: seção "V3__create_indexes.sql" da documentação.

## Checklist

- [ ] Arquivos nomeados exatamente com prefixo `V` + número + `__` + descrição + `.sql`
- [ ] Versões criadas: V1, V2, V3 — NÃO criar V4 aqui (gerada no prompt `database/02-migrations-outbox.md`)
- [ ] `CHECK (duracao_min > 0)` presente na tabela `filme`
- [ ] `UNIQUE (sala_id, fileira, numero)` presente na tabela `assento`
- [ ] `UNIQUE (sessao_id, assento_id)` presente na tabela `sessao_assento`
- [ ] Índice parcial `WHERE status = 'PENDENTE'` na tabela `outbox_events`
