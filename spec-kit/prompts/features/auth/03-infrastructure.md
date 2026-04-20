---
context:
  - docs/architecture/layer-rules.md
  - docs/features/auth.md
  - docs/database/tables.md
---

# Tarefa: Implementar infrastructure/security e persistence/usuario

## infrastructure/persistence/usuario/

### UsuarioJpaEntity.java
- `@Entity @Table(name = "usuario")`
- Campos espelham a tabela `usuario` em `docs/database/tables.md`
- `role` como `@Enumerated(EnumType.STRING)`

### UsuarioJpaRepository.java
- `Optional<UsuarioJpaEntity> findByEmail(String email)`
- `boolean existsByEmail(String email)`

### UsuarioRepositoryAdapter.java
- `@Repository implements UsuarioRepository`
- Converte `Email` value object para `String` ao chamar o JPA
- Usa `UsuarioJpaMapper` para conversão

### UsuarioJpaMapper.java
- `Usuario toDomainEntity(UsuarioJpaEntity entity)`
- `UsuarioJpaEntity toJpaEntity(Usuario usuario)`

---

## infrastructure/security/

### JwtUtil.java
- `@Component implements JwtPort`
- Injeta: `RedisTemplate<String, Object>` (para blacklist), `@Value("${jwt.secret}") String secret`
- `gerar(Usuario usuario)`:
  - Cria JWT com claims: `sub = email`, `role = usuario.getRole().name()`, `userId = usuario.getId().valor()`
  - Expira em 15 minutos
  - Retorna `TokenResult`
- `extrairEmail(String token)`: extrai claim `sub`
- `isValido(String token)`: valida assinatura + expiração + não está na blacklist
- `revogar(String token)`:
  - Extrai JTI do token
  - `redisTemplate.opsForValue().set("token:blacklist:" + jti, "1", tempoRestante, SECONDS)`
- `isRevogado(String token)`:
  - `redisTemplate.hasKey("token:blacklist:" + jti)`

### JwtAuthFilter.java
- `extends OncePerRequestFilter`
- Extrai token do header `Authorization: Bearer <token>`
- Valida via `JwtPort.isValido()` e `JwtPort.isRevogado()`
- Popula `SecurityContextHolder` com `UsernamePasswordAuthenticationToken`
- Claims `userId` e `role` devem estar nos authorities

### SecurityConfig.java
- Rotas públicas, ADMIN e autenticadas conforme `docs/features/auth.md`
- `@Bean PasswordEncoder` → retorna `new BCryptPasswordEncoder(12)`
- Session management: STATELESS
- Adiciona `JwtAuthFilter` antes de `UsernamePasswordAuthenticationFilter`

### SpringUserDetailsAdapter.java
- `implements UserDetailsService`
- `loadUserByUsername(String email)`:
  - `UsuarioRepository.findByEmail(new Email(email))`
  - → `UsernameNotFoundException` se ausente
  - Retorna `org.springframework.security.core.userdetails.User` com authorities

## Checklist

- [ ] `JwtUtil` implementa `JwtPort` (não é uma classe standalone)
- [ ] Secret JWT vem de variável de ambiente, não hardcodado
- [ ] Blacklist usa TTL calculado como tempo restante do token (não TTL fixo)
- [ ] `BCryptPasswordEncoder` com custo 12
- [ ] `SecurityConfig` declara `PasswordEncoder` como `@Bean`
