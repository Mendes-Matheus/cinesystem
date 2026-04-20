---
context:
  - docs/architecture/layer-rules.md
  - docs/features/sessao.md
---

# Tarefa: Implementar interfaces/http/sessao

## SessaoRequestDTO.java
```java
public record SessaoRequestDTO(
    @NotNull Long filmeId,
    @NotNull Long salaId,
    @NotNull @Future LocalDateTime dataHora,
    @NotBlank String idioma,
    @NotNull FormatoExibicao formato,
    @NotNull @Positive BigDecimal preco
) {}
```

## SessaoResponseDTO.java e AssentoResponseDTO.java
- `record` espelhando `SessaoResult` e `AssentoResult` respectivamente

## SessaoHttpMapper.java
- `CriarSessaoCommand toCommand(SessaoRequestDTO dto)`
- `SessaoResponseDTO toResponse(SessaoResult result)`
- `List<SessaoResponseDTO> toResponseList(List<SessaoResult> results)`
- `AssentoResponseDTO toAssentoResponse(AssentoResult result)`
- `List<AssentoResponseDTO> toAssentoResponseList(List<AssentoResult> results)`

## SessaoController.java
- `@RestController`
- Dois `@RequestMapping` base: `/api/v1/filmes` e `/api/v1/sessoes`
- Injeta: `ListarSessoesPorFilmeUseCase`, `BuscarAssentosUseCase`,
  `CriarSessaoUseCase`, `CancelarSessaoUseCase`, `SessaoHttpMapper`

### Endpoints
```java
// GET /api/v1/filmes/{filmeId}/sessoes  — público
@GetMapping("/api/v1/filmes/{filmeId}/sessoes")
ResponseEntity<List<SessaoResponseDTO>> listarPorFilme(@PathVariable Long filmeId)

// GET /api/v1/sessoes/{id}/assentos  — autenticado
@GetMapping("/api/v1/sessoes/{id}/assentos")
ResponseEntity<List<AssentoResponseDTO>> listarAssentos(@PathVariable Long id)

// POST /api/v1/sessoes  — ADMIN
@PostMapping("/api/v1/sessoes")
@PreAuthorize("hasRole('ADMIN')")
ResponseEntity<SessaoResponseDTO> criar(@Valid @RequestBody SessaoRequestDTO dto)

// DELETE /api/v1/sessoes/{id}  — ADMIN
@DeleteMapping("/api/v1/sessoes/{id}")
@PreAuthorize("hasRole('ADMIN')")
ResponseEntity<Void> cancelar(@PathVariable Long id)
```

## Checklist

- [ ] `@Future` presente em `SessaoRequestDTO.dataHora`
- [ ] GET de assentos exige autenticação mas não exige ADMIN
- [ ] Controller não referencia nenhuma classe de `infrastructure/`
