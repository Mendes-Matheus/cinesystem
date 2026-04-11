---
context:
  - docs/architecture/layer-rules.md
  - docs/features/sessao.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar application/sessao

## DTOs — application/sessao/dto/

### CriarSessaoCommand.java
- `record` conforme `docs/features/sessao.md`

### SessaoResult.java e AssentoResult.java
- `record` com campos especificados, incluindo método `from()` estático

## Portas de leitura — application/port/out/query/

### SessaoQueryPort.java
- Interface com os 2 métodos: `findAtivasByFilme` e `findAssentosBySessao`

## Use Cases

### ListarSessoesPorFilmeUseCaseImpl
- Usa `SessaoQueryPort` (CQRS — não carrega entidade Sessao)
- Cache: chave `"sessoes:filme:{filmeId.valor()}"`, TTL 5 minutos
- `CachePort.get()` → se miss → `SessaoQueryPort.findAtivasByFilme()` → `CachePort.set()`

### BuscarAssentosUseCaseImpl
- Usa `SessaoQueryPort.findAssentosBySessao()`
- Cache: chave `"assentos:sessao:{sessaoId.id()}"`, TTL 30 segundos
- TTL curto porque status de assento muda com cada reserva Redis

### CriarSessaoUseCaseImpl
- `@Transactional`
- Valida que `FilmeRepository.findById(command.filmeId())` existe
- Cria `Sessao` via construtor (validações de domínio)
- Salva via `SessaoRepository`
- `CachePort.evictByPrefix("sessoes:filme:")`  ← evicta todas as listas de sessões
- Gera assentos automaticamente via `gerarAssentosDaSala()` (lê sala e cria SessaoAssento para cada assento)

### CancelarSessaoUseCaseImpl
- `@Transactional`
- Carrega `Sessao` via `SessaoRepository.findById()`
- Chama `sessao.cancelar()` → DomainException se não for ATIVA
- Salva, evicta cache `"sessoes:filme:{sessao.getFilmeId().valor()}"`

## Checklist

- [ ] `ListarSessoesPorFilmeUseCaseImpl` usa `SessaoQueryPort`, não `SessaoRepository`
- [ ] `BuscarAssentosUseCaseImpl` usa TTL de 30 segundos (não 15 minutos)
- [ ] `CriarSessaoUseCaseImpl` faz evict de `"sessoes:filme:"` com prefix
- [ ] Todos os commands e results são `record`
