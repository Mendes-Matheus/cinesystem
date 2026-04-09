# Feature: Autenticação

## Domínio

### Entidade: Usuario
```
id          UsuarioId
nome        String          NOT NULL
email       Email           Value Object — valida formato
senha       Senha           Value Object — encapsula BCrypt hash
role        Role            CLIENTE | ADMIN
ativo       boolean         default true
criadoEm    LocalDateTime
```

### Value Objects
```java
// Email — validação de formato no construtor
public record Email(String valor) {
    public Email {
        if (valor == null || !valor.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$"))
            throw new DomainException("E-mail inválido: " + valor);
    }
}

// Senha — nunca armazena texto claro, apenas hash
public record Senha(String hash) {
    public static Senha criar(String textoClaro, PasswordEncoder encoder) {
        return new Senha(encoder.encode(textoClaro));
    }
    public boolean matches(String textoClaro, PasswordEncoder encoder) {
        return encoder.matches(textoClaro, this.hash);
    }
}
```

### Interface de repositório
```java
public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
```

---

## Application

### Use Cases
| Interface | Porta de entrada |
|-----------|-----------------|
| `CadastrarUsuarioUseCase` | `execute(CadastroCommand): TokenResult` |
| `AutenticarUsuarioUseCase` | `execute(LoginCommand): TokenResult` |

### Commands e Result
```java
public record CadastroCommand(String nome, String email, String senha) {}
public record LoginCommand(String email, String senha) {}

public record TokenResult(
    String accessToken, String tokenType, Long expiresIn
) {
    // tokenType é sempre "Bearer"
}}
```

### Comportamento de CadastrarUsuarioUseCaseImpl
1. Valida unicidade: `usuarioRepository.existsByEmail(new Email(command.email()))`
   → se existir: `DomainException("E-mail já cadastrado")`
2. Cria `Senha.criar(command.senha(), passwordEncoder)`
3. Cria `Usuario` com `Role.CLIENTE`
4. `usuarioRepository.save(usuario)`
5. Gera e retorna `TokenResult` via `JwtPort.gerar(usuario)`

### Comportamento de AutenticarUsuarioUseCaseImpl
1. `usuarioRepository.findByEmail(new Email(command.email()))`
   → se ausente: `DomainException("Credenciais inválidas")` (mensagem genérica — não revela se e-mail existe)
2. Valida senha: `usuario.getSenha().matches(command.senha(), passwordEncoder)`
   → se inválida: mesma `DomainException("Credenciais inválidas")`
3. Valida `usuario.isAtivo()` → `DomainException("Conta desativada")`
4. Retorna `JwtPort.gerar(usuario)`

---

## Infrastructure: Security

### SecurityConfig — rotas públicas vs protegidas
```
Públicas:   /api/v1/auth/**
            /api/v1/filmes/**
            /api/v1/filmes/{id}/sessoes
            /v3/api-docs/**  (Swagger)

ADMIN:      /api/v1/admin/**

Demais:     autenticado (qualquer role)
```

### JWT
- Access token: 15 minutos
- Algoritmo: HS256 com secret via variável de ambiente `JWT_SECRET`
- Claims: `sub` (email), `role`, `userId`
- Blacklist de tokens revogados: Redis — chave `"token:blacklist:{jti}"` com TTL = tempo restante do token

---

## Interface (HTTP)

| Método | Path | Auth | Body | Response |
|--------|------|------|------|----------|
| POST | `/api/v1/auth/cadastro` | Pública | `CadastroRequestDTO` | `AuthResponseDTO` (201) |
| POST | `/api/v1/auth/login` | Pública | `LoginRequestDTO` | `AuthResponseDTO` |
| POST | `/api/v1/auth/logout` | Autenticado | — | 204 |

### AuthResponseDTO
```java
public record AuthResponseDTO(
    String accessToken, String tokenType, Long expiresIn
) {}
```
// tokenType é sempre "Bearer" — padronizado com TokenResult
