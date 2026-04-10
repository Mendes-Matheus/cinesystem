---
context:
  - docs/architecture/layer-rules.md
  - docs/features/ingresso.md
---

# Tarefa: Implementar interfaces/http/ingresso

Implemente os 4 arquivos da camada HTTP do módulo Ingresso.

## IngressoRequestDTO.java
```java
public record IngressoRequestDTO(
    @NotNull Long sessaoId,
    @NotNull Long assentoId,
    @NotNull MetodoPagamento metodoPagamento
) {}
```

## DTOs de resposta

### IngressoBasicoResponseDTO.java
- `record` espelhando `IngressoBasicoResult` — usado na resposta do POST (compra)
- Campos: `id`, `codigo`, `valorPago`, `status`, `compradoEm`

### IngressoResponseDTO.java
- `record` espelhando `IngressoResult` — usado nas respostas de listagem e busca
- Campos: `id`, `codigo`, `sessaoId`, `assentoId`, `fileira`, `numeroAssento`,
  `tituloFilme`, `dataHora`, `valorPago`, `status`

## IngressoHttpMapper.java
- `@Component`
- `ComprarIngressoCommand toCommand(IngressoRequestDTO dto, Long usuarioId)`
  — `usuarioId` vem do `SecurityContextHolder`, não do body
- `IngressoBasicoResponseDTO toBasicoResponse(IngressoBasicoResult result)`
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
- Retorna `ResponseEntity<IngressoBasicoResponseDTO>` com status **201 Created**

### Endpoint DELETE /api/v1/ingressos/{id}
- Constrói `CancelarIngressoCommand(new IngressoId(id), new UsuarioId(getUsuarioAutenticado()))`
- Retorna **204 No Content**

### Endpoint GET /api/v1/ingressos/meus
- Chama `ListarMeusIngressosUseCase.execute(new UsuarioId(getUsuarioAutenticado()))`
- Retorna `ResponseEntity<List<IngressoResponseDTO>>` com status **200**

### Endpoint GET /api/v1/ingressos/{id}
- Chama `BuscarIngressoPorIdUseCase.execute(new IngressoId(id), new UsuarioId(getUsuarioAutenticado()))`
- Retorna `ResponseEntity<IngressoResponseDTO>` com status **200**

## Checklist

- [ ] `usuarioId` nunca vem do request body — sempre do contexto de segurança
- [ ] Controller não injeta nenhuma implementação concreta
- [ ] POST retorna `IngressoBasicoResponseDTO` (201) — não `IngressoResponseDTO`
- [ ] GET `/meus` e GET `/{id}` retornam `IngressoResponseDTO` (200)
- [ ] DELETE retorna 204 sem body
