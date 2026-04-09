---
context:
  - docs/architecture/clean-architecture.md
  - docs/features/filme.md
  - docs/features/ingresso.md
  - docs/conventions/testing.md
---

# Tarefa: Gerar testes de integração com Testcontainers

Gere os testes de integração para os dois fluxos principais do sistema.
Os testes ficam em `src/test/java/com/cinesystem/`.

## Setup base — CineSystemIntegrationTest.java (classe abstrata)

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class CineSystemIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cinesystem_test");

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired protected TestRestTemplate restTemplate;
}
```

---

## FilmeIntegrationTest.java

Testa o fluxo completo de filme via HTTP.

### Cenários obrigatórios

1. `deveListarFilmesVazia_QuandoNenhumCadastrado()`
   - GET `/api/v1/filmes` → 200 com lista vazia

2. `deveCriarFilme_ComAutenticacaoAdmin()`
   - Autentica como admin
   - POST `/api/v1/filmes` com payload válido → 201
   - GET `/api/v1/filmes/{id}` → 200 com dados corretos

3. `deveRetornar403_QuandoClienteTentaCriarFilme()`
   - Autentica como cliente
   - POST `/api/v1/filmes` → 403

4. `deveRetornar422_QuandoDadosInvalidos()`
   - POST `/api/v1/filmes` com `duracaoMinutos: -1` → 422 com `DOMAIN_ERROR`

---

## ComprarIngressoIntegrationTest.java

Testa o fluxo completo de compra.

### Cenários obrigatórios

1. `deveComprarIngresso_ERegistrarNoOutbox()`
   - Arrange: cria filme, sala, sessão, registra assentos
   - Autentica usuário
   - POST `/api/v1/ingressos` → 201
   - Verifica banco: `ingresso` com status ATIVO
   - Verifica banco: `outbox_events` com `event_type = 'IngressoComprado'` e `status = 'PENDENTE'`

2. `deveRetornar422_QuandoAssentoJaReservado()`
   - Reserva o mesmo assento via Redis manualmente antes do teste
   - POST `/api/v1/ingressos` → 422 com `DOMAIN_ERROR`

3. `deveCancelarIngresso_QuandoUsuarioEhDono()`
   - Compra ingresso
   - DELETE `/api/v1/ingressos/{id}` → 204
   - GET `/api/v1/ingressos/{id}` → ingresso com status CANCELADO

4. `deveRetornar403_QuandoOutroUsuarioTentaCancelar()`
   - Compra ingresso como usuário A
   - Tenta cancelar como usuário B → 403

## Convenções

- Sempre limpar banco entre testes com `@Transactional` + `@Rollback`
  ou com `@Sql(scripts = "/test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)`
- Helpers de autenticação: `loginComoAdmin()` e `loginComoCliente()` retornam token JWT
- Usar `restTemplate.withBasicAuth()` ou adicionar header `Authorization: Bearer {token}`
