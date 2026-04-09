---
context:
  - docs/architecture/layer-rules.md
  - docs/features/auth.md
  - docs/conventions/patterns.md
---

# Tarefa: Configurar infrastructure/security e infrastructure/config

Implemente os arquivos de configuração e segurança globais do projeto.
Estes arquivos são o esqueleto da infraestrutura — sem lógica de negócio.

---

## infrastructure/config/

### JpaConfig.java
```java
@Configuration
@EnableJpaRepositories(basePackages = "com.cinesystem.infrastructure.persistence")
@EntityScan(basePackages = "com.cinesystem.infrastructure.persistence")
public class JpaConfig {}
```

### SwaggerConfig.java
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CineSystem API")
                .version("1.0")
                .description("API do sistema de cinema"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

---

## infrastructure/cache/RedisConfig.java

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

---

## infrastructure/cache/RedisCacheAdapter.java

- `@Component implements CachePort`
- Injeta `RedisTemplate<String, Object>`
- Implementa todos os 4 métodos da interface `CachePort`:
  ```java
  void set(String key, Object value, Duration ttl)
  Optional<Object> get(String key)
  void evict(String key)
  void evictByPrefix(String keyPrefix)
  ```
- `evictByPrefix`: usa `redisTemplate.keys(keyPrefix + "*")` + `redisTemplate.delete(keys)`
  - Se o conjunto de chaves for vazio, não faz nada (evita NullPointerException)

---

## interfaces/exception/GlobalExceptionHandler.java

Implemente o handler global conforme a tabela em `docs/architecture/layer-rules.md`.

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponseDTO> handleDomain(DomainException ex) {
        // status 422, código "DOMAIN_ERROR"
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        // status 404, código "NOT_FOUND"
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        // status 400, código "VALIDATION_ERROR"
        // popula detalhes com "field: message" para cada FieldError
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbidden(AccessDeniedException ex) {
        // status 403, código "FORBIDDEN"
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        // status 500, código "INTERNAL_ERROR"
        // log.error("Erro não tratado", ex)  ← loga stack trace, não expõe ao cliente
    }
}
```

---

## interfaces/exception/ErrorResponseDTO.java e ResourceNotFoundException.java

### ErrorResponseDTO.java
```java
public record ErrorResponseDTO(
    String codigo,
    String mensagem,
    LocalDateTime timestamp,
    List<String> detalhes
) {
    // Construtor de conveniência sem detalhes
    public ErrorResponseDTO(String codigo, String mensagem) {
        this(codigo, mensagem, LocalDateTime.now(), List.of());
    }
}
```

### ResourceNotFoundException.java
```java
// Fica em domain/shared/ — é uma exceção de domínio especializada
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
```

---

## application.yml — configurações base

Crie `src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: cinesystem

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/cinesystem}
    username: ${DB_USER:cineuser}
    password: ${DB_PASS:cinepass}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
      hibernate.jdbc.time_zone: UTC

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

jwt:
  secret: ${JWT_SECRET:dev-secret-minimo-256-bits-nunca-usar-em-producao}
  expiration-minutes: 15

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

Crie também `src/main/resources/application-docker.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/cinesystem
  data:
    redis:
      host: redis
```

---

## Checklist

- [ ] `RedisCacheAdapter.evictByPrefix()` não lança exceção se nenhuma chave corresponder
- [ ] `GlobalExceptionHandler` loga stack trace apenas no fallback `Exception`
- [ ] `ResourceNotFoundException` está em `domain/shared/`, não em `infrastructure/`
- [ ] `application.yml` usa variáveis de ambiente com valores default para dev local
- [ ] `application-docker.yml` sobrescreve apenas o que muda no ambiente Docker
