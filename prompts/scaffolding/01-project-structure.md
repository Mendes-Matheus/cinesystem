---
context:
  - docs/architecture/package-structure.md
  - docs/architecture/layer-rules.md
---

# Tarefa: Criar estrutura de pacotes e arquivos vazios

Crie todos os pacotes e arquivos Java do projeto CineSystem
conforme a estrutura exata definida em `docs/architecture/package-structure.md`.

## Regras

- Crie cada arquivo com apenas o `package`, imports mínimos e a
  declaração da classe/interface/record — SEM implementação de método
- Para interfaces: declare apenas as assinaturas dos métodos
- Para classes abstratas: declare apenas os campos e construtores
- Para records: declare apenas os campos
- Para enums: declare apenas os valores listados na doc
- Não implemente nenhuma lógica ainda

## Ordem de criação

1. `domain/shared/` — DomainException, AggregateRoot, DomainEvent
2. `domain/` — todos os módulos (filme, sessao, assento, ingresso, pagamento, usuario)
3. `application/port/out/` — todas as interfaces de porta
4. `application/` — todos os use cases (interface + impl vazia) e DTOs
5. `infrastructure/` — todos os adaptadores (classe vazia implementando a interface)
6. `interfaces/` — controllers, schedulers, exception handler

## Confirmação

Após criar cada pacote, liste os arquivos criados antes de prosseguir
para o próximo pacote.
