# Convenções de Nomenclatura

## Pacotes

| Camada | Padrão | Exemplo |
|--------|--------|---------|
| Domínio | `com.cinesystem.domain.{modulo}` | `com.cinesystem.domain.filme` |
| Aplicação | `com.cinesystem.application.{modulo}` | `com.cinesystem.application.ingresso` |
| Infraestrutura | `com.cinesystem.infrastructure.{tipo}` | `com.cinesystem.infrastructure.persistence.filme` |
| Interfaces | `com.cinesystem.interfaces.http.{modulo}` | `com.cinesystem.interfaces.http.sessao` |

---

## Classes por tipo

| Tipo | Sufixo | Exemplo |
|------|--------|---------|
| Entidade de domínio | (sem sufixo) | `Filme`, `Ingresso`, `Usuario` |
| Value Object | (sem sufixo) | `Email`, `CodigoIngresso`, `FilmeId` |
| Interface de repositório (domínio) | `Repository` | `FilmeRepository` |
| Interface de porta de leitura | `QueryPort` | `FilmeQueryPort` |
| Interface de porta de saída | `Port` | `CachePort`, `EmailPort` |
| Interface de caso de uso | `UseCase` | `CriarFilmeUseCase` |
| Implementação de caso de uso | `UseCaseImpl` | `CriarFilmeUseCaseImpl` |
| Adaptador JPA (@Entity) | `JpaEntity` | `FilmeJpaEntity` |
| Repositório Spring Data | `JpaRepository` | `FilmeJpaRepository` |
| Adaptador de repositório | `RepositoryAdapter` | `FilmeRepositoryAdapter` |
| Adaptador de query (CQRS) | `QueryAdapter` | `FilmeQueryAdapter` |
| Mapper domínio ↔ JPA | `JpaMapper` | `FilmeJpaMapper` |
| Comando (record) | `Command` | `CriarFilmeCommand` |
| Resultado (record) | `Result` | `FilmeResult` |
| DTO de request HTTP | `RequestDTO` | `FilmeRequestDTO` |
| DTO de response HTTP | `ResponseDTO` | `FilmeResponseDTO` |
| Mapper HTTP ↔ Command | `HttpMapper` | `FilmeHttpMapper` |
| Controller REST | `Controller` | `FilmeController` |
| Scheduler | `Scheduler` | `OutboxProcessorScheduler` |
| Evento de domínio | `Event` | `FilmeCriadoEvent`, `IngressoCompradoEvent` |
| Payload de Outbox | `Payload` | `IngressoCompradoPayload` |

---

## Métodos

### Use Cases
- Interface: `execute(...)` — sempre um único método público
- Nome da interface descreve o caso de uso: `CriarFilmeUseCase.execute(CriarFilmeCommand)`

### Repositórios (domínio)
- `save(Entity)` — criação e atualização
- `findById(Id)` → `Optional<Entity>`
- `findAll...()` → `List<Entity>`
- `delete(Id)` — remoção
- `existsBy...()` → `boolean`

### QueryPorts
- `findResultById(Id)` → `Optional<Result>` — distingue de `findById` do repositório
- `findAllAtivos(filtro)` → `List<Result>`
- `findPaginado(filtro, pageable)` → `Page<Result>`

### Adaptadores JPA
- Nomes que espelham o repositório de domínio: `findByAtivoTrue()`, `findBySalaId()`
- Projeções com prefixo `findProjected...()`: `findProjectedAtivos()`, `findProjectedById()`

### Testes
- `deve[Comportamento]_Quando[Condicao]()`
- Exemplos: `deveLancarExcecao_QuandoAssentoIndisponivel()`, `deveCriarFilme_EEvictarCache()`

---

## Banco de dados

| Elemento | Padrão | Exemplo |
|----------|--------|---------|
| Tabelas | `snake_case`, plural | `sessao_assento`, `outbox_events` |
| Colunas | `snake_case` | `valor_pago`, `criado_em` |
| PKs | `id` (sempre) | `id BIGSERIAL PRIMARY KEY` |
| FKs | `{tabela_referenciada}_id` | `filme_id`, `usuario_id` |
| Índices | `idx_{tabela}_{coluna(s)}` | `idx_sessao_filme_data` |
| Migrations | `V{n}__{descricao}.sql` | `V3__create_indexes.sql` |
| Enums (valores) | `SCREAMING_SNAKE_CASE` | `'DISPONIVEL'`, `'CARTAO_CREDITO'` |

---

## Variáveis de ambiente

| Variável | Descrição | Default (dev) |
|----------|-----------|---------------|
| `DB_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/cinesystem` |
| `DB_USER` | Usuário do banco | `cineuser` |
| `DB_PASS` | Senha do banco | `cinepass` |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PORT` | Porta do Redis | `6379` |
| `JWT_SECRET` | Secret HMAC-SHA256 (mín. 256 bits) | `dev-secret-...` |

---

## Idioma

- **Domínio de negócio** → português: `Ingresso`, `Sessao`, `Sala`, `FilmeId`
- **Infraestrutura e padrões** → inglês: `Repository`, `Adapter`, `UseCase`, `Controller`
- **Banco de dados** → português: `ingresso`, `sessao_assento`, `criado_em`
- **Variáveis e métodos** → português quando representam conceito de negócio: `valorPago`, `dataHora`
