---
context:
  - docs/architecture/layer-rules.md
  - docs/features/admin.md
  - docs/conventions/naming.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar módulo Admin completo

Leia `docs/features/admin.md` na íntegra antes de começar.
O módulo Admin não tem domínio próprio — orquestra use cases existentes
e adiciona 3 novos use cases de leitura/gestão.

---

## Parte 1 — Novos use cases em application/

### application/port/out/query/UsuarioQueryPort.java
```java
public interface UsuarioQueryPort {
    Page<UsuarioResult> findAll(Pageable pageable);
    Optional<UsuarioResult> findResultById(UsuarioId id);
}
```

### application/usuario/dto/UsuarioResult.java
```java
// ATENÇÃO: nunca incluir senhaHash neste record
public record UsuarioResult(
    Long id, String nome, String email,
    String role, boolean ativo, LocalDateTime criadoEm
) {}
```

### application/sessao/dto/RelatorioSessaoResult.java
```java
public record RelatorioSessaoResult(
    Long sessaoId, String tituloFilme, LocalDateTime dataHora,
    int totalAssentos, int assentosOcupados, int assentosDisponiveis,
    BigDecimal receitaTotal
) {}
```

### application/usuario/usecase/ListarUsuariosUseCase.java e Impl
- Interface: `execute(Pageable pageable): Page<UsuarioResult>`
- Impl: injeta `UsuarioQueryPort` (não `UsuarioRepository`)
- Sem cache — dados administrativos são sensíveis e devem ser frescos

### application/usuario/usecase/DesativarUsuarioUseCase.java e Impl
- Interface: `execute(UsuarioId alvoId, UsuarioId adminId): void`
- Impl injeta `UsuarioRepository`
- Regras:
  1. `alvoId.equals(adminId)` → `DomainException("Administrador não pode desativar a própria conta")`
  2. `usuarioRepository.findById(alvoId)` → `ResourceNotFoundException` se ausente
  3. `usuario.desativar()` → `DomainException` se já inativo
  4. `usuarioRepository.save(usuario)`

### application/sessao/usecase/RelatorioSessaoUseCase.java e Impl
- Interface: `execute(SessaoId id): RelatorioSessaoResult`
- Impl injeta `SessaoQueryPort` e `IngressoQueryPort`
- Usa projeções — não carrega entidade Sessão

---

## Parte 2 — Adaptador de leitura de usuário em infrastructure/

### infrastructure/persistence/usuario/UsuarioQueryAdapter.java
- `@Repository implements UsuarioQueryPort`
- Injeta `UsuarioJpaRepository`
- Adicione ao `UsuarioJpaRepository` a query de projeção:
  ```java
  @Query("""
      SELECT new com.cinesystem.application.usuario.dto.UsuarioResult(
          u.id, u.nome, u.email, u.role, u.ativo, u.criadoEm
      )
      FROM UsuarioJpaEntity u
      ORDER BY u.criadoEm DESC
      """)
  Page<UsuarioResult> findAllProjected(Pageable pageable);
  ```
- **NUNCA incluir `senha_hash` na projeção**

---

## Parte 3 — Controller em interfaces/

### interfaces/http/admin/AdminController.java
- `@RestController @RequestMapping("/api/v1/admin")`
- `@PreAuthorize("hasRole('ADMIN')")` no nível da **classe** (protege todos os endpoints)
- Injeta:
  - `ListarUsuariosUseCase`
  - `DesativarUsuarioUseCase`
  - `RelatorioSessaoUseCase`
  - `AdminHttpMapper`

### Endpoints
```java
// GET /api/v1/admin/usuarios?page=0&size=20
@GetMapping("/usuarios")
ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size)

// DELETE /api/v1/admin/usuarios/{id}   — retorna 204
@DeleteMapping("/usuarios/{id}")
ResponseEntity<Void> desativarUsuario(@PathVariable Long id, Authentication auth)
// Extrai adminId do objeto Authentication para passar ao use case

// GET /api/v1/admin/sessoes/{id}/relatorio
@GetMapping("/sessoes/{id}/relatorio")
ResponseEntity<RelatorioSessaoResponseDTO> relatorioDaSessao(@PathVariable Long id)
```

### interfaces/http/admin/AdminHttpMapper.java
- `UsuarioResponseDTO toUsuarioResponse(UsuarioResult result)`
- `Page<UsuarioResponseDTO> toUsuarioPage(Page<UsuarioResult> page)`
- `RelatorioSessaoResponseDTO toRelatorioResponse(RelatorioSessaoResult result)`

### interfaces/http/admin/UsuarioResponseDTO.java
```java
// sem senhaHash
public record UsuarioResponseDTO(
    Long id, String nome, String email,
    String role, boolean ativo, LocalDateTime criadoEm
) {}
```

### interfaces/http/admin/RelatorioSessaoResponseDTO.java
```java
public record RelatorioSessaoResponseDTO(
    Long sessaoId, String tituloFilme, String dataHora,
    int totalAssentos, int assentosOcupados, int assentosDisponiveis,
    BigDecimal receitaTotal
) {}
```

---

## Checklist

- [ ] `@PreAuthorize("hasRole('ADMIN')")` está no nível da CLASSE, não de cada método
- [ ] `DesativarUsuarioUseCaseImpl` recebe `adminId` e valida auto-desativação
- [ ] `UsuarioQueryAdapter` nunca projeta `senha_hash`
- [ ] `AdminController` extrai `adminId` do `Authentication`, não do request body
- [ ] `UsuarioResponseDTO` não tem campo `senhaHash`
- [ ] `ListarUsuariosUseCaseImpl` usa `UsuarioQueryPort`, não `UsuarioRepository`
