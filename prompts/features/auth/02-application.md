---
context:
  - docs/architecture/layer-rules.md
  - docs/features/auth.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar application/auth

## DTOs — application/auth/dto/

### CadastroCommand.java
- `record CadastroCommand(String nome, String email, String senha)`

### LoginCommand.java
- `record LoginCommand(String email, String senha)`

### TokenResult.java
- `record TokenResult(String accessToken, String tokenType, Long expiresIn)`
- `tokenType` é sempre `"Bearer"`

## Porta de saída — application/port/out/

### JwtPort.java
```java
public interface JwtPort {
    TokenResult gerar(Usuario usuario);
    String extrairEmail(String token);
    boolean isValido(String token);
    void revogar(String token);     // adiciona à blacklist Redis
    boolean isRevogado(String token);
}
```

## Use Cases — application/auth/usecase/

### CadastrarUsuarioUseCaseImpl
- `@Service @Transactional`
- Injeta: `UsuarioRepository`, `PasswordEncoder`, `JwtPort`
- Fluxo exato da seção "Comportamento de CadastrarUsuarioUseCaseImpl" em `docs/features/auth.md`
- Mensagem de erro de e-mail duplicado: `"E-mail já cadastrado"`
- Retorna `TokenResult` (usuário já sai logado após cadastro)

### AutenticarUsuarioUseCaseImpl
- `@Service`
- Injeta: `UsuarioRepository`, `PasswordEncoder`, `JwtPort`
- Fluxo exato da seção "Comportamento de AutenticarUsuarioUseCaseImpl" em `docs/features/auth.md`
- IMPORTANTE: mensagem de erro de credenciais sempre genérica: `"Credenciais inválidas"`
  — nunca revelar se o e-mail existe ou não

## Checklist

- [ ] `CadastrarUsuarioUseCaseImpl` não informa qual campo está errado (e-mail vs senha)
  na validação de login — apenas "Credenciais inválidas"
- [ ] `AutenticarUsuarioUseCaseImpl` chama `Senha.matches()` passando `PasswordEncoder`
- [ ] Nenhum use case injeta `JwtUtil` diretamente — usa `JwtPort`
- [ ] `PasswordEncoder` é injetado como interface (não `BCryptPasswordEncoder`)
