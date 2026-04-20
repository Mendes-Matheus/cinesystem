---
context:
  - docs/architecture/layer-rules.md
  - docs/features/auth.md
---

# Tarefa: Implementar interfaces/http/auth e interfaces/exception/

## interfaces/http/auth/

### AuthRequestDTO.java
```java
public record AuthRequestDTO(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String senha
) {}
```

### CadastroRequestDTO.java
```java
public record CadastroRequestDTO(
    @NotBlank String nome,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String senha
) {}
```

### AuthResponseDTO.java
```java
public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    Long expiresIn
) {}
```

### AuthHttpMapper.java
- `CadastroCommand toCommand(CadastroRequestDTO dto)`
- `LoginCommand toCommand(AuthRequestDTO dto)`
- `AuthResponseDTO toResponse(TokenResult result)`

### AuthController.java
- `@RestController @RequestMapping("/api/v1/auth")`
- Injeta: `CadastrarUsuarioUseCase`, `AutenticarUsuarioUseCase`, `JwtPort`, `AuthHttpMapper`

```java
// POST /api/v1/auth/cadastro — público — retorna 201
@PostMapping("/cadastro")
ResponseEntity<AuthResponseDTO> cadastrar(@Valid @RequestBody CadastroRequestDTO dto)

// POST /api/v1/auth/login — público — retorna 200
@PostMapping("/login")
ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto)

// POST /api/v1/auth/logout — autenticado — retorna 204
@PostMapping("/logout")
ResponseEntity<Void> logout(HttpServletRequest request)
// logout extrai o token do header Authorization e chama JwtPort.revogar(token)
```

---

## interfaces/exception/

### ErrorResponseDTO.java
```java
public record ErrorResponseDTO(
    String codigo,
    String mensagem,
    LocalDateTime timestamp,
    List<String> detalhes
) {
    public ErrorResponseDTO(String codigo, String mensagem) {
        this(codigo, mensagem, LocalDateTime.now(), List.of());
    }
}
```

### GlobalExceptionHandler.java
- `@RestControllerAdvice`
- Handlers conforme tabela em `docs/architecture/layer-rules.md`:

| Exceção                           | Status | Código        |
|-----------------------------------|--------|---------------|
| `DomainException`                 | 422    | `DOMAIN_ERROR`|
| `ResourceNotFoundException`       | 404    | `NOT_FOUND`   |
| `MethodArgumentNotValidException` | 400    | `VALIDATION_ERROR` |
| `AccessDeniedException`           | 403    | `FORBIDDEN`   |
| `Exception` (fallback)            | 500    | `INTERNAL_ERROR` |

- Para `MethodArgumentNotValidException`: popular `detalhes` com lista de `field: message`
- Para os demais: `detalhes` vazio

## Checklist

- [ ] `logout` extrai token do header, não do body
- [ ] `GlobalExceptionHandler` nunca expõe stack trace no response
- [ ] Fallback `Exception` loga o erro antes de retornar 500
- [ ] `ErrorResponseDTO.timestamp` usa `LocalDateTime.now()` no construtor
