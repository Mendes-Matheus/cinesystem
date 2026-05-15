---
context:
  - sdd-kit/docs/architecture/layer-rules.md
  - sdd-kit/docs/features/filme.md
  - sdd-kit/docs/conventions/patterns.md
---

# Tarefa: Implementar interfaces/http/filme

Implemente os 4 arquivos da camada de interface HTTP do módulo Filme.

## FilmeRequestDTO.java
- `record` com validações Jakarta Bean Validation conforme `sdd-kit/docs/features/filme.md`
- `@NotBlank`, `@NotNull`, `@Positive` nos campos corretos

## FilmeResponseDTO.java
- `record` que espelha `FilmeResult` — campos para serialização HTTP

## FilmeHttpMapper.java
- `@Component`
- `CriarFilmeCommand toCommand(FilmeRequestDTO dto)`
- `AtualizarFilmeCommand toUpdateCommand(Long id, FilmeRequestDTO dto)`
- `FilmeResponseDTO toResponse(FilmeResult result)`
- `List<FilmeResponseDTO> toResponseList(List<FilmeResult> results)`

## FilmeController.java
- `@RestController @RequestMapping("/api/v1/filmes")`
- Injeta: `ListarFilmesUseCase`, `BuscarFilmePorIdUseCase`, `CriarFilmeUseCase`,
  `AtualizarFilmeUseCase`, `FilmeHttpMapper`
- ATENÇÃO: injeta SOMENTE interfaces de use case — nunca implementações ou repositórios
- Implemente os 5 endpoints conforme a tabela em `sdd-kit/docs/features/filme.md`
- `@PreAuthorize("hasRole('ADMIN')")` nos endpoints POST, PUT, DELETE

## Checklist

- [ ] Controller não injeta nenhuma classe de `infrastructure/`
- [ ] Controller não injeta nenhuma implementação concreta (`...UseCaseImpl`, `...Adapter`)
- [ ] `FilmeRequestDTO` tem `@Valid` no parâmetro do controller
- [ ] DELETE retorna `ResponseEntity<Void>` com status 204
- [ ] GET público não tem `@PreAuthorize`
