---
context:
  - docs/architecture/clean-architecture.md
  - docs/architecture/layer-rules.md
  - docs/features/filme.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar application/filme

Implemente a camada de aplicação do módulo Filme.
Os arquivos de domínio (Filme, FilmeId, FilmeRepository) já existem.

## DTOs — application/filme/dto/

### FilmeResult.java
- `record` com todos os campos listados em `docs/features/filme.md`
- Método estático `from(Filme filme)` que mapeia domínio → result

### CriarFilmeCommand.java
- `record` com os campos especificados

### AtualizarFilmeCommand.java
- `record` incluindo `FilmeId id` como primeiro campo

## Portas de leitura — application/port/out/query/

### FilmeQueryPort.java
- Interface com os 2 métodos especificados em `docs/features/filme.md`
- ATENÇÃO: esta porta retorna `FilmeResult` diretamente — nunca `Filme`

## Use Cases — application/filme/usecase/

Implemente os 4 pares interface + impl conforme a tabela em `docs/features/filme.md`.

### Comportamentos obrigatórios

**ListarFilmesUseCaseImpl:**
- Usa `FilmeQueryPort` (CQRS — NÃO usa `FilmeRepository`)
- Lógica de cache conforme descrito na seção "Comportamento de ListarFilmesUseCaseImpl"
- Cache key: `"filmes:listagem:" + (genero != null ? genero.toLowerCase() : "todos")`

**CriarFilmeUseCaseImpl:**
- Fluxo exato da seção "Comportamento de CriarFilmeUseCaseImpl"
- `@Transactional` na implementação
- `CachePort.evictByPrefix("filmes:listagem:")` após salvar

**BuscarFilmePorIdUseCaseImpl:**
- Usa `FilmeQueryPort.findResultById()`
- Lança `ResourceNotFoundException("Filme não encontrado: " + id)` se ausente

**AtualizarFilmeUseCaseImpl:**
- Carrega `Filme` via `FilmeRepository.findById()` (escrita — precisa da entidade)
- Aplica mudanças, salva, evicta cache

## Checklist

- [ ] Nenhuma impl importa `RedisTemplate`, `JpaRepository` ou classe de `infrastructure/`
- [ ] `ListarFilmesUseCaseImpl` usa `FilmeQueryPort`, não `FilmeRepository`
- [ ] `CriarFilmeUseCaseImpl` tem `@Transactional`
- [ ] Todos os commands e results são `record`
