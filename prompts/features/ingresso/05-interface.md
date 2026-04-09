---
context:
  - docs/architecture/layer-rules.md
  - docs/features/ingresso.md
---

# Tarefa: Implementar interfaces/http/ingresso

Implemente os 4 arquivos da camada HTTP do módulo Ingresso.

## ComprarIngressoRequestDTO.java
```java
public record ComprarIngressoRequestDTO(
    @NotNull Long sessaoId,
    @NotNull Long assentoId,
    @NotNull MetodoPagamento metodoPagamento
) {}
```

## IngressoResponseDTO.java
- `record` que espelha todos os campos de `IngressoResult`

## IngressoHttpMapper.java
- `@Component`
- `ComprarIngressoCommand toCommand(ComprarIngressoRequestDTO dto, Long usuarioId)`
  — `usuarioId` vem do `SecurityContextHolder`, não do body
- `IngressoResponseDTO toResponse(IngressoResult result)`
- `List<IngressoResponseDTO> toResponseList(List<IngressoResult> results)`

## IngressoController.java
- `@RestController @RequestMapping("/api/v1/ingressos")`
- Injeta: `ComprarIngressoUseCase`, `CancelarIngressoUseCase`,
  `ListarMeusIngressosUseCase`, `BuscarIngressoPorIdUseCase`, `IngressoHttpMapper`
- Implementa os 4 endpoints da tabela em `docs/features/ingresso.md`

### Extração do usuário autenticado (helper privado)
```java
private Long getUsuarioAutenticado() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    // extrai userId dos claims do JWT (já populado pelo JwtAuthFilter)
    return Long.parseLong(auth.getName());
}
```

### Endpoint POST /api/v1/ingressos
- Extrai `usuarioId` via `getUsuarioAutenticado()`
- Constrói `ComprarIngressoCommand` via mapper passando o `usuarioId`
- Retorna 201 Created

### Endpoint DELETE /api/v1/ingressos/{id}
- Constrói `CancelarIngressoCommand(new IngressoId(id), new UsuarioId(getUsuarioAutenticado()))`
- Retorna 204 No Content

### Endpoint GET /api/v1/ingressos/meus
- Chama `ListarMeusIngressosUseCase.execute(new UsuarioId(getUsuarioAutenticado()))`
- Retorna 200 com lista

## Checklist

- [ ] `usuarioId` nunca vem do request body — sempre do contexto de segurança
- [ ] Controller não injeta nenhuma implementação concreta
- [ ] DELETE retorna 204
- [ ] POST retorna 201
