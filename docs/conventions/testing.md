# Convenções de Teste

## Regra geral: mock nas portas, nunca nas implementações

```java
@Mock FilmeRepository filmeRepository;          // interface do domínio — correto
@Mock FilmeQueryPort filmeQueryPort;            // porta de leitura — correto
@Mock FilmeRepositoryAdapter filmeAdapter;      // implementação concreta — ERRADO
```

## Testes de Domínio — zero mocks, zero Spring

```java
class FilmeTest {
    @Test void deveRejeitarTituloVazio() {
        assertThatThrownBy(() -> new Filme("", Genero.ACAO, ClassificacaoEtaria.L, 90))
            .isInstanceOf(DomainException.class);
    }
}
```

## Testes de Use Case — mock nas portas

```java
@ExtendWith(MockitoExtension.class)
class CriarFilmeUseCaseTest {
    @Mock FilmeRepository filmeRepository;
    @Mock CachePort cachePort;
    @Mock ApplicationEventPublisher publisher;
    @InjectMocks CriarFilmeUseCaseImpl useCase;

    @Test void deveCriarFilmeEEvictarCache() {
        when(filmeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var result = useCase.execute(new CriarFilmeCommand("Duna", FICCAO, A12, 156, null, LocalDate.now()));
        assertThat(result.titulo()).isEqualTo("Duna");
        verify(cachePort).evictByPrefix("filmes:listagem:");
    }
}
```

## Testes de Integração — Testcontainers

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class IngressoIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

## Nomenclatura de métodos de teste

```
deve[Comportamento]Quando[Condição]

deveLancarExcecaoQuandoAssentoIndisponivel()
deveGerarIngressoQuandoAssentoDisponivel()
deveEvictarCacheAoCriarFilme()
```
