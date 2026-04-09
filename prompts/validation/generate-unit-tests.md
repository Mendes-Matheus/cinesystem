---
context:
  - docs/architecture/layer-rules.md
  - docs/conventions/testing.md
  - docs/features/ingresso.md
  - docs/features/sessao.md
  - docs/features/auth.md
---

# Tarefa: Gerar testes unitários para todos os módulos

Gere os arquivos de teste em `src/test/java/com/cinesystem/`.
Siga estritamente as convenções em `docs/conventions/testing.md`.

---

## 1. Testes de Domínio (zero mocks, zero Spring)

### domain/ingresso/IngressoTest.java
```
deveCriarIngressoComDadosValidos()
deveGerarCodigoUUIDAutomaticamente_QuandoNaoFornecido()
deveCancelarIngressoAtivo()
deveLancarExcecao_QuandoCancelarIngressoJaCancelado()
deveLancarExcecao_QuandoCancelarIngressoUtilizado()
deveMarcarIngressoComoUtilizado()
deveLancarExcecao_QuandoValorPagoZeroOuNegativo()
```

### domain/sessao/SessaoTest.java
```
deveCriarSessaoComDadosValidos()
deveLancarExcecao_QuandoDataHoraNoPassado()
deveLancarExcecao_QuandoPrecoZero()
deveCancelarSessaoAtiva()
deveLancarExcecao_QuandoCancelarSessaoJaCancelada()
```

### domain/usuario/EmailTest.java
```
deveAceitarEmailValido()
deveRejeitarEmailSemArroba()
deveRejeitarEmailSemDominio()
deveRejeitarEmailNulo()
```

### domain/ingresso/CodigoIngressoTest.java
```
deveGerarCodigosUnicos_AoChamarGerarDuasVezes()
deveRejeitarCodigoVazio()
deveRejeitarCodigoNulo()
```

---

## 2. Testes de Use Cases (mock nas portas)

### application/filme/ListarFilmesUseCaseTest.java

Setup:
```java
@Mock FilmeQueryPort filmeQueryPort;
@Mock CachePort cachePort;
@InjectMocks ListarFilmesUseCaseImpl useCase;
```

Cenários:
```
deveRetornarDoCache_QuandoCacheHit()
  - cachePort.get() retorna Optional com lista de FilmeResult
  - filmeQueryPort NÃO deve ser chamado (verify never)

deveConsultarQueryPort_EPopularCache_QuandoCacheMiss()
  - cachePort.get() retorna Optional.empty()
  - filmeQueryPort.findAllAtivos() retorna lista
  - cachePort.set() deve ser chamado com TTL de 15 minutos

deveUsarCacheKeyCorreta_QuandoGeneroInformado()
  - genero = "ACAO" → cache key = "filmes:listagem:acao"

deveUsarCacheKeyCorreta_QuandoGeneroNulo()
  - genero = null → cache key = "filmes:listagem:todos"
```

### application/filme/CriarFilmeUseCaseTest.java

Setup:
```java
@Mock FilmeRepository filmeRepository;
@Mock CachePort cachePort;
@Mock ApplicationEventPublisher publisher;
@InjectMocks CriarFilmeUseCaseImpl useCase;
```

Cenários:
```
deveCriarFilme_EEvictarCache_EPublicarEvento()
  - filmeRepository.save() retorna filme mockado
  - cachePort.evictByPrefix("filmes:listagem:") deve ser chamado
  - publisher.publishEvent() deve ser chamado com FilmeCriadoEvent
  - resultado deve ter mesmo título do command

devePropagar_DomainException_QuandoDadosInvalidos()
  - command com título vazio → DomainException propagada
  - filmeRepository.save() NÃO deve ser chamado
```

### application/ingresso/ComprarIngressoUseCaseTest.java

Setup:
```java
@Mock SessaoRepository sessaoRepository;
@Mock IngressoRepository ingressoRepository;
@Mock OutboxRepository outboxRepository;
@Mock ReservaAssentoPort reservaPort;
@InjectMocks ComprarIngressoUseCaseImpl useCase;
```

Cenários:
```
deveComprarIngresso_ERegistrarNoOutbox_QuandoAssentoDisponivel()
  - sessaoRepository.findSessaoAssento() retorna SessaoAssento disponível
  - reservaPort.reservar() retorna true
  - ingressoRepository.save() retorna ingresso mockado
  - outboxRepository.save() DEVE ser chamado (verify once)
  - resultado com status ATIVO

deveLancarDomainException_QuandoAssentoIndisponivel()
  - reservaPort.reservar() retorna false
  - ingressoRepository.save() NÃO deve ser chamado (verify never)
  - outboxRepository.save() NÃO deve ser chamado (verify never)

deveLancarDomainException_QuandoSessaoAssentoNaoEncontrado()
  - sessaoRepository.findSessaoAssento() retorna Optional.empty()
  - ResourceNotFoundException lançada
```

### application/auth/AutenticarUsuarioUseCaseTest.java

Setup:
```java
@Mock UsuarioRepository usuarioRepository;
@Mock PasswordEncoder passwordEncoder;
@Mock JwtPort jwtPort;
@InjectMocks AutenticarUsuarioUseCaseImpl useCase;
```

Cenários:
```
deveAutenticar_QuandoCredenciaisValidas()
  - usuarioRepository.findByEmail() retorna usuario ativo
  - passwordEncoder.matches() retorna true
  - jwtPort.gerar() retorna TokenResult
  - resultado deve conter accessToken

deveLancarDomainException_QuandoEmailNaoEncontrado()
  - usuarioRepository.findByEmail() retorna Optional.empty()
  - mensagem de erro: "Credenciais inválidas" (nunca revelar que email não existe)

deveLancarDomainException_QuandoSenhaIncorreta()
  - passwordEncoder.matches() retorna false
  - mesma mensagem genérica: "Credenciais inválidas"

deveLancarDomainException_QuandoContaDesativada()
  - usuario.isAtivo() retorna false
  - mensagem: "Conta desativada"
```

---

## 3. Testes do OutboxProcessorScheduler

### interfaces/scheduler/OutboxProcessorSchedulerTest.java

Setup:
```java
@Mock OutboxRepository outboxRepository;
@Mock EmailPort emailPort;
@Mock ObjectMapper objectMapper;
@InjectMocks OutboxProcessorScheduler scheduler;
```

Cenários:
```
deveProcessarEventosPendentes_EMarcarComoProcessado()
  - outboxRepository.findPendentes(50) retorna lista com 1 evento
  - emailPort.enviarConfirmacaoIngresso() chamado sem exceção
  - outboxRepository.save() chamado com evento com status PROCESSADO

deveRegistrarFalha_QuandoEmailPortLancaExcecao()
  - emailPort.enviarConfirmacaoIngresso() lança RuntimeException
  - outboxRepository.save() chamado com evento com status FALHA e tentativas++
  - scheduler NÃO relança a exceção (absorve a falha)

deveProcessarMultiplosEventos_EmUmCiclo()
  - findPendentes retorna lista com 3 eventos
  - outboxRepository.save() chamado 3 vezes
```

---

## Convenções obrigatórias em todos os testes

```java
// Estrutura padrão de cada teste
@Test
@DisplayName("descrição legível do comportamento")
void deve[Comportamento]_Quando[Condicao]() {
    // arrange
    ...

    // act
    var result = useCase.execute(command);

    // assert
    assertThat(result.campo()).isEqualTo(valorEsperado);
    verify(porta).metodo(any());          // verificação de interação
    verify(outraPorta, never()).metodo(); // verificação de não-interação
}
```

- Usar `@DisplayName` em todos os métodos de teste
- `assertThat` de AssertJ (não `assertEquals` do JUnit)
- `verify(mock, never())` para confirmar que algo NÃO aconteceu
- Nunca mockar classe concreta — sempre interface
