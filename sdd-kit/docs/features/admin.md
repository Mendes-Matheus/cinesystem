# Feature: Admin

## Escopo

O módulo Admin não tem domínio próprio — ele orquestra use cases
já existentes (Filme, Sessão, Sala) através de um controller dedicado
com autorização restrita a `ROLE_ADMIN`.

Não crie novos use cases exclusivos de admin, salvo os listados abaixo.

---

## Use Cases que o Admin consome (já implementados)

| Ação | Use Case |
|------|----------|
| Criar/Editar/Remover filme | `CriarFilmeUseCase`, `AtualizarFilmeUseCase` |
| Criar/Cancelar sessão | `CriarSessaoUseCase`, `CancelarSessaoUseCase` |
| Ver relatório de ingressos por sessão | `RelatorioSessaoUseCase` *(novo — ver abaixo)* |
| Listar todos os usuários | `ListarUsuariosUseCase` *(novo — ver abaixo)* |
| Desativar usuário | `DesativarUsuarioUseCase` *(novo — ver abaixo)* |

---

## Novos Use Cases para Admin

### RelatorioSessaoUseCase
- Porta de entrada: `execute(SessaoId): RelatorioSessaoResult`
- Usa `SessaoQueryPort` + `IngressoQueryPort` — leitura pura, sem entidade de domínio
- `RelatorioSessaoResult` (record):
  ```
  Long sessaoId, String tituloFilme, LocalDateTime dataHora,
  int totalAssentos, int assentosOcupados, int assentosDisponiveis,
  BigDecimal receitaTotal
  ```

### ListarUsuariosUseCase
- Porta de entrada: `execute(int page, int size): Page<UsuarioResult>`
- Usa `UsuarioQueryPort` (criar junto)
- `UsuarioResult` (record):
  ```
  Long id, String nome, String email, String role,
  boolean ativo, LocalDateTime criadoEm
  ```
- **Nunca retorna `senha_hash`** — campo excluído na projeção

### DesativarUsuarioUseCase
- Porta de entrada: `execute(UsuarioId): void`
- Carrega `Usuario` via `UsuarioRepository.findById()`
- Chama `usuario.desativar()` → `DomainException` se já inativo
- Salva via `UsuarioRepository`

---

## Porta de leitura: UsuarioQueryPort

```java
public interface UsuarioQueryPort {
    Page<UsuarioResult> findAll(Pageable pageable);
    Optional<UsuarioResult> findResultById(UsuarioId id);
}
```

Implementada por `UsuarioQueryAdapter` em `infrastructure/persistence/usuario/`.
Query JPQL projeta direto em `UsuarioResult` — nunca inclui `senha_hash`.

---

## Interface (HTTP): AdminController

`@RestController @RequestMapping("/api/v1/admin")`
`@PreAuthorize("hasRole('ADMIN')")` — aplica a todos os endpoints da classe

| Método | Path | Body/Param | Response |
|--------|------|-----------|----------|
| GET | `/api/v1/admin/usuarios` | `?page=0&size=20` | `Page<UsuarioResponseDTO>` |
| DELETE | `/api/v1/admin/usuarios/{id}` | — | 204 |
| GET | `/api/v1/admin/sessoes/{id}/relatorio` | — | `RelatorioSessaoResponseDTO` |

### AdminController injeta
- `ListarUsuariosUseCase`
- `DesativarUsuarioUseCase`
- `RelatorioSessaoUseCase`
- `AdminHttpMapper`

### AdminHttpMapper
- `UsuarioResponseDTO toUsuarioResponse(UsuarioResult result)`
- `RelatorioSessaoResponseDTO toRelatorioResponse(RelatorioSessaoResult result)`

---

## Regras de segurança

- Administrador **não pode se desativar** — valide que `usuarioId != adminLogadoId`
  → `DomainException("Administrador não pode desativar a própria conta")`
- Endpoint de relatório expõe dados financeiros — garantir `hasRole('ADMIN')` no nível do método
- `UsuarioResponseDTO` nunca deve conter `senhaHash`
