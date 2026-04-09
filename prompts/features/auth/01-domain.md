---
context:
  - docs/architecture/layer-rules.md
  - docs/features/auth.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar domain/usuario

## Arquivos a criar

### UsuarioId.java
- `record UsuarioId(Long valor)` — rejeita null

### Role.java
- `enum`: `CLIENTE, ADMIN`

### Email.java
- `record Email(String valor)`
- Compact constructor valida regex: `^[\w.-]+@[\w.-]+\.[a-z]{2,}$`
- → `DomainException("E-mail inválido: " + valor)` se não bater

### Senha.java
- `record Senha(String hash)`
- Método estático: `static Senha criar(String textoClaro, PasswordEncoder encoder)`
  — PasswordEncoder vem de `org.springframework.security.crypto.password.PasswordEncoder`
  — ESTE é o único import de Spring permitido em domain/ para este Value Object
  — Justificativa: BCrypt é uma função de hash — não é comportamento de framework
- Método de instância: `boolean matches(String textoClaro, PasswordEncoder encoder)`

### Usuario.java
- POJO puro (exceto o import de PasswordEncoder via Senha)
- Campos conforme `docs/features/auth.md`
- Construtor valida: nome não vazio, email não nulo, senha não nula, role não nula
- Método `desativar()`: valida ativo == true → `DomainException("Conta já desativada")`
- Getters para todos os campos

### UsuarioRepository.java
- Interface com 3 métodos: `save`, `findByEmail`, `existsByEmail`

## Checklist

- [ ] `Email` rejeita `"semArroba"` e `"teste@"` com DomainException
- [ ] `Senha.criar()` delega para `encoder.encode()` — não implementa hash
- [ ] `Usuario` não tem setters públicos
- [ ] `UsuarioRepository` é interface sem anotações
