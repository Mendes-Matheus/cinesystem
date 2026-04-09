---
context:
  - docs/architecture/clean-architecture.md
  - docs/architecture/layer-rules.md
---

# Tarefa: Auditoria global da Regra de Dependência

Percorra TODOS os arquivos Java do projeto e reporte qualquer violação
da Regra de Dependência da Clean Architecture.

## O que procurar

### Violações críticas (corrija imediatamente)

1. Qualquer arquivo em `domain/` com import de:
   - `org.springframework.*` **— EXCETO** `PasswordEncoder` em `Senha.java`
     (exceção documentada em `docs/architecture/layer-rules.md`)
   - `jakarta.persistence.*`
   - Qualquer classe de `infrastructure/` ou `interfaces/`

2. Qualquer arquivo em `application/` com import de:
   - `jakarta.persistence.*`
   - Qualquer classe concreta de `infrastructure/` (ex: `FilmeJpaRepository`, `RedisTemplate`)
   - Qualquer classe de `interfaces/`

3. Qualquer arquivo em `interfaces/` com import de:
   - Implementações concretas de `infrastructure/` (ex: `FilmeRepositoryAdapter`)
   - Implementações concretas de `application/` (ex: `CriarFilmeUseCaseImpl`)

4. Qualquer `Controller` que injeta algo diferente de interfaces de use case

### Violações de padrão (corrija antes de prosseguir)

5. Use case de listagem/busca que usa `FilmeRepository` em vez de `FilmeQueryPort`
6. `OutboxRepository.save()` fora de um método `@Transactional`
7. `record` com campos mutáveis (campo não-final ou com setter)
8. Adaptador que implementa mais de uma interface de porta

## Formato do relatório

Para cada violação encontrada:
```
VIOLAÇÃO [CRÍTICA|PADRÃO]
Arquivo: com.cinesystem.application.filme.usecase.ListarFilmesUseCaseImpl
Linha: 12
Import proibido: com.cinesystem.infrastructure.persistence.filme.FilmeJpaRepository
Correção: substituir por FilmeQueryPort (porta de leitura)
```

Após listar todas as violações, aplique as correções e confirme
que o projeto compila sem erros (`mvn compile`).
