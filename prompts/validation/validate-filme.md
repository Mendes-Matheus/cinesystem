---
context:
  - docs/architecture/clean-architecture.md
  - docs/architecture/layer-rules.md
  - docs/conventions/patterns.md
  - docs/conventions/testing.md
---

# Tarefa: Validar módulo Filme e gerar testes unitários

## Parte 1 — Auditoria de dependências

Verifique cada arquivo do módulo Filme e reporte violações da regra de dependência.

### Verificações obrigatórias

**domain/filme/**
- [ ] Nenhum arquivo tem import de `org.springframework.*`
- [ ] Nenhum arquivo tem import de `jakarta.persistence.*`
- [ ] `FilmeRepository` é interface sem anotações

**application/filme/**
- [ ] `ListarFilmesUseCaseImpl` injeta `FilmeQueryPort`, não `FilmeRepository`
- [ ] `CriarFilmeUseCaseImpl` injeta `FilmeRepository` (escrita) e `CachePort`
- [ ] Nenhuma impl injeta `FilmeJpaRepository`, `RedisTemplate` ou classe de infra
- [ ] Todos os commands e results são `record`

**infrastructure/persistence/filme/**
- [ ] `FilmeRepositoryAdapter` implementa `FilmeRepository` (não `FilmeQueryPort`)
- [ ] `FilmeQueryAdapter` implementa `FilmeQueryPort` (não `FilmeRepository`)
- [ ] `FilmeQueryAdapter` não usa `FilmeJpaMapper`

**interfaces/http/filme/**
- [ ] `FilmeController` injeta apenas interfaces (não `...Impl`, não `...Adapter`)

Se encontrar violações, corrija antes de prosseguir para a Parte 2.

---

## Parte 2 — Gerar testes unitários

Gere os testes unitários para:

### FilmeTest.java (`domain/filme/`)
Testa invariantes da entidade sem nenhum mock:
- `deveCriarFilmeComDadosValidos()`
- `deveRejeitarTituloVazio()`
- `deveRejeitarDuracaoZero()`
- `deveDesativarFilmeAtivo()`
- `deveLancarExcecaoAoDesativarFilmeInativo()`

### ListarFilmesUseCaseTest.java (`application/filme/`)
- Mock em `FilmeQueryPort` e `CachePort`
- `deveRetornarDoCache_QuandoCacheHit()`
- `deveConsultarBancoEPopularCache_QuandoCacheMiss()`
- `deveFiltrارPorGenero_QuandoGeneroInformado()`

### CriarFilmeUseCaseTest.java (`application/filme/`)
- Mock em `FilmeRepository`, `CachePort`, `ApplicationEventPublisher`
- `deveCriarFilme_EEvictarCache_EPublicarEvento()`
- `deveLancarExcecao_QuandoDominioRejeita()`

### Convenções de nomenclatura
- Método: `deve[Comportamento]_Quando[Condição]()`
- Seção: `// arrange`, `// act`, `// assert`
- Nunca mock em classe concreta — sempre em interface
