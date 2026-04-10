# Estrutura de Pacotes — Backend

Base: `src/main/java/com/cinesystem/`

```
com.cinesystem/
├── CineSystemApplication.java
│
├── domain/
│   ├── filme/
│   │   ├── Filme.java                      # Entidade raiz do agregado
│   │   ├── FilmeId.java                    # Value Object
│   │   ├── Genero.java                     # Enum de domínio
│   │   ├── ClassificacaoEtaria.java        # Value Object com validação
│   │   └── FilmeRepository.java            # Interface (porta de saída)
│   ├── sessao/
│   │   ├── Sessao.java
│   │   ├── SessaoId.java
│   │   ├── SessaoAssento.java
│   │   ├── StatusSessao.java
│   │   ├── FormatoExibicao.java
│   │   └── SessaoRepository.java
│   ├── assento/
│   │   ├── Assento.java
│   │   ├── AssentoId.java
│   │   ├── TipoAssento.java
│   │   ├── StatusAssento.java
│   │   └── AssentoRepository.java
│   ├── ingresso/
│   │   ├── Ingresso.java
│   │   ├── IngressoId.java
│   │   ├── CodigoIngresso.java             # Value Object (UUID encapsulado)
│   │   ├── StatusIngresso.java
│   │   └── IngressoRepository.java
│   ├── pagamento/
│   │   ├── Pagamento.java
│   │   ├── MetodoPagamento.java
│   │   ├── StatusPagamento.java
│   │   └── PagamentoRepository.java
│   ├── usuario/
│   │   ├── Usuario.java
│   │   ├── UsuarioId.java
│   │   ├── Email.java                      # Value Object com validação de formato
│   │   ├── Senha.java                      # Value Object (encapsula hash BCrypt)
│   │   ├── Role.java
│   │   └── UsuarioRepository.java
│   └── shared/
│       ├── DomainException.java
│       ├── AggregateRoot.java
│       └── DomainEvent.java
│
├── application/
│   ├── filme/
│   │   ├── usecase/
│   │   │   ├── ListarFilmesUseCase.java
│   │   │   ├── ListarFilmesUseCaseImpl.java
│   │   │   ├── BuscarFilmePorIdUseCase.java
│   │   │   ├── BuscarFilmePorIdUseCaseImpl.java
│   │   │   ├── CriarFilmeUseCase.java
│   │   │   ├── CriarFilmeUseCaseImpl.java
│   │   │   ├── AtualizarFilmeUseCase.java
│   │   │   └── AtualizarFilmeUseCaseImpl.java
│   │   └── dto/
│   │       ├── CriarFilmeCommand.java      # record
│   │       ├── AtualizarFilmeCommand.java  # record
│   │       └── FilmeResult.java            # record
│   ├── sessao/
│   │   ├── usecase/
│   │   │   ├── ListarSessoesPorFilmeUseCase.java
│   │   │   ├── ListarSessoesPorFilmeUseCaseImpl.java
│   │   │   ├── CriarSessaoUseCase.java
│   │   │   └── CriarSessaoUseCaseImpl.java
│   │   └── dto/
│   │       ├── CriarSessaoCommand.java
│   │       └── SessaoResult.java
│   ├── ingresso/
│   │   ├── usecase/
│   │   │   ├── ComprarIngressoUseCase.java
│   │   │   ├── ComprarIngressoUseCaseImpl.java  # usa Outbox + ReservaAssentoPort
│   │   │   ├── CancelarIngressoUseCase.java
│   │   │   ├── CancelarIngressoUseCaseImpl.java
│   │   │   ├── ListarMeusIngressosUseCase.java
│   │   │   ├── ListarMeusIngressosUseCaseImpl.java
│   │   │   ├── BuscarIngressoPorIdUseCase.java
│   │   │   └── BuscarIngressoPorIdUseCaseImpl.java
│   │   └── dto/
│   │       ├── ComprarIngressoCommand.java
│   │       ├── CancelarIngressoCommand.java
│   │       └── IngressoResult.java
│   ├── auth/
│   │   ├── usecase/
│   │   │   ├── CadastrarUsuarioUseCase.java
│   │   │   ├── CadastrarUsuarioUseCaseImpl.java
│   │   │   ├── AutenticarUsuarioUseCase.java
│   │   │   └── AutenticarUsuarioUseCaseImpl.java
│   │   └── dto/
│   │       ├── CadastroCommand.java
│   │       ├── LoginCommand.java
│   │       └── TokenResult.java
│   ├── outbox/
│   │   ├── OutboxEvent.java                # Entidade (não é de domínio de negócio)
│   │   ├── OutboxRepository.java           # Interface
│   │   └── IngressoCompradoPayload.java    # record
│   └── port/
│       └── out/
│           ├── CachePort.java
│           ├── EmailPort.java
│           ├── PagamentoGatewayPort.java
│           ├── ReservaAssentoPort.java
│           └── query/
│               ├── FilmeQueryPort.java
│               ├── SessaoQueryPort.java
│               └── IngressoQueryPort.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── filme/
│   │   │   ├── FilmeJpaEntity.java
│   │   │   ├── FilmeJpaRepository.java
│   │   │   ├── FilmeRepositoryAdapter.java  # implements FilmeRepository
│   │   │   ├── FilmeQueryAdapter.java       # implements FilmeQueryPort
│   │   │   └── FilmeJpaMapper.java
│   │   ├── sessao/
│   │   │   ├── SessaoJpaEntity.java
│   │   │   ├── SessaoJpaRepository.java
│   │   │   ├── SessaoRepositoryAdapter.java
│   │   │   ├── SessaoQueryAdapter.java
│   │   │   └── SessaoJpaMapper.java
│   │   ├── ingresso/
│   │   │   ├── IngressoJpaEntity.java
│   │   │   ├── IngressoJpaRepository.java
│   │   │   ├── IngressoRepositoryAdapter.java
│   │   │   ├── IngressoQueryAdapter.java
│   │   │   └── IngressoJpaMapper.java
│   │   ├── usuario/
│   │   │   ├── UsuarioJpaEntity.java
│   │   │   ├── UsuarioJpaRepository.java
│   │   │   ├── UsuarioRepositoryAdapter.java
│   │   │   └── UsuarioJpaMapper.java
│   │   └── outbox/
│   │       ├── OutboxEventJpaEntity.java
│   │       ├── OutboxJpaRepository.java
│   │       └── OutboxRepositoryAdapter.java
│   ├── cache/
│   │   ├── RedisCacheAdapter.java          # implements CachePort
│   │   ├── RedisReservaAdapter.java        # implements ReservaAssentoPort
│   │   └── RedisConfig.java
│   ├── security/
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthFilter.java
│   │   ├── SecurityConfig.java
│   │   └── SpringUserDetailsAdapter.java
│   ├── email/
│   │   └── JavaMailEmailAdapter.java       # implements EmailPort
│   ├── pagamento/
│   │   └── MercadoPagoGatewayAdapter.java  # implements PagamentoGatewayPort
│   └── config/
│       ├── JpaConfig.java
│       └── SwaggerConfig.java
│
└── interfaces/
    ├── http/
    │   ├── auth/
    │   │   ├── AuthController.java
    │   │   ├── AuthRequestDTO.java
    │   │   ├── AuthResponseDTO.java
    │   │   └── AuthHttpMapper.java
    │   ├── filme/
    │   │   ├── FilmeController.java
    │   │   ├── FilmeRequestDTO.java
    │   │   ├── FilmeResponseDTO.java
    │   │   └── FilmeHttpMapper.java
    │   ├── sessao/
    │   │   ├── SessaoController.java
    │   │   ├── SessaoRequestDTO.java
    │   │   ├── SessaoResponseDTO.java
    │   │   └── SessaoHttpMapper.java
    │   ├── ingresso/
    │   │   ├── IngressoController.java
    │   │   ├── IngressoRequestDTO.java   IngressoBasicoResult IngressoResult IngressoBasicoResponseDTO
    │   │   ├── IngressoBasicoResponseDTO.java
    │   │   ├── IngressoBasicoResult.java
    │   │   ├── IngressoResult.java
    │   │   ├── IngressoResponseDTO.java
    │   │   └── IngressoHttpMapper.java
    │   └── admin/
    │       └── AdminController.java
    ├── scheduler/
    │   ├── ReservaExpiradaScheduler.java
    │   └── OutboxProcessorScheduler.java
    └── exception/
        ├── GlobalExceptionHandler.java
        └── ErrorResponseDTO.java
```
