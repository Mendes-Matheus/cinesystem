---
context:
  - docs/architecture/clean-architecture.md
  - docs/architecture/layer-rules.md
  - docs/features/ingresso.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar application/ingresso (Outbox Pattern)

Esta é a feature mais crítica do sistema. Leia `docs/features/ingresso.md`
completo antes de começar, especialmente a seção "Outbox Pattern — OBRIGATÓRIO".

## Arquivos a criar em application/outbox/

### OutboxEvent.java
- Classe (não record — precisa de métodos mutadores para `marcarProcessado` e `registrarFalha`)
- Campos conforme `docs/features/ingresso.md`
- Método estático `of(String type, String aggregateId, Object payload)`:
  serializa `payload` para JSON com Jackson `ObjectMapper`
- `marcarProcessado()`: seta status="PROCESSADO" e processadoEm=now()
- `registrarFalha(String motivo)`: incrementa tentativas, seta status="FALHA"

### IngressoCompradoPayload.java
- `record` com os campos especificados

### OutboxRepository.java
- Interface com `save()` e `findPendentes(int limit)`

## Arquivos a criar em application/ingresso/

### ComprarIngressoCommand.java e CancelarIngressoCommand.java
- `record` conforme especificado

### IngressoBasicoResult.java e IngressoResult.java

Crie **dois** records conforme `docs/features/ingresso.md` (seção Results):

- `IngressoBasicoResult` — retornado por `ComprarIngressoUseCase`
  - Campos: `id`, `codigo`, `valorPago`, `status`, `compradoEm`
  - Tem método estático `from(Ingresso ingresso)` — é possível porque usa apenas dados do agregado
- `IngressoResult` — retornado pelos use cases de leitura
  - Campos: `id`, `codigo`, `sessaoId`, `assentoId`, `fileira`, `numeroAssento`, `tituloFilme`, `dataHora`, `valorPago`, `status`
  - **SEM** `from(Ingresso)` — construído pelo adaptador JPA via projeção JPQL

### IngressoQueryPort.java
- Interface com os 2 métodos de leitura (retornam `IngressoResult`)

### ComprarIngressoUseCase.java e CancelarIngressoUseCase.java
- Interfaces (portas de entrada)
- `ComprarIngressoUseCase`: `execute(ComprarIngressoCommand): IngressoBasicoResult`
- `CancelarIngressoUseCase`: `execute(CancelarIngressoCommand): void`

### ListarMeusIngressosUseCase.java e BuscarIngressoPorIdUseCase.java
- `ListarMeusIngressosUseCase`: `execute(UsuarioId): List<IngressoResult>`
  - Impl usa `IngressoQueryPort.findByUsuario()` — sem cache
- `BuscarIngressoPorIdUseCase`: `execute(IngressoId, UsuarioId): IngressoResult`
  - Impl valida que o ingresso pertence ao usuário antes de retornar

### ComprarIngressoUseCaseImpl.java
- `@Service @Transactional`
- Fluxo EXATO de 6 passos definido em `docs/features/ingresso.md`
- Passo 5 (OutboxEvent) DEVE estar dentro da mesma `@Transactional` que o passo 4 (save ingresso)
- NÃO use `ApplicationEventPublisher`
- Retorna `IngressoBasicoResult.from(salvo)`

### CancelarIngressoUseCaseImpl.java
- Carrega ingresso por ID via `IngressoRepository.findById()`
- Valida que `ingresso.getUsuarioId().equals(command.usuarioId())` → `DomainException("Acesso negado")`
- Chama `ingresso.cancelar()` → DomainException se status != ATIVO
- Salva via `IngressoRepository`

## Checklist

- [ ] `ComprarIngressoUseCaseImpl` tem `OutboxRepository` injetado (não `ApplicationEventPublisher`)
- [ ] O `outboxRepository.save()` está DENTRO do mesmo método `@Transactional`
- [ ] Nenhum impl importa `RedisTemplate` ou `JpaRepository`
- [ ] `OutboxEvent.of()` serializa o payload para String JSON
- [ ] `findPendentes` retorna lista limitada por `limit` ordenada por `criadoEm ASC`
- [ ] `IngressoResult` NÃO tem método `from(Ingresso)` — é construído pela query JPA
- [ ] `IngressoBasicoResult` TEM método `from(Ingresso)` — usa apenas campos do agregado
