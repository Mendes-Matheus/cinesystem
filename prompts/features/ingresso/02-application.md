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

### IngressoResult.java
- `record` com método estático `from(Ingresso ingresso)`

### IngressoQueryPort.java
- Interface com os 2 métodos de leitura

### ComprarIngressoUseCase.java e CancelarIngressoUseCase.java
- Interfaces (portas de entrada)

### ComprarIngressoUseCaseImpl.java
- `@Service @Transactional`
- Fluxo EXATO de 6 passos definido em `docs/features/ingresso.md`
- Passo 5 (OutboxEvent) DEVE estar dentro da mesma `@Transactional` que o passo 4 (save ingresso)
- NÃO use `ApplicationEventPublisher`

### CancelarIngressoUseCaseImpl.java
- Carrega ingresso por ID
- Valida que `ingresso.getUsuarioId().equals(command.usuarioId())` → 403 se não for dono
- Chama `ingresso.cancelar()` → DomainException se status != ATIVO
- Salva e libera reserva Redis via `ReservaAssentoPort.liberar()`

## Checklist

- [ ] `ComprarIngressoUseCaseImpl` tem `OutboxRepository` injetado (não `ApplicationEventPublisher`)
- [ ] O `outboxRepository.save()` está DENTRO do mesmo método `@Transactional`
- [ ] Nenhum impl importa `RedisTemplate` ou `JpaRepository`
- [ ] `OutboxEvent.of()` serializa o payload para String JSON
- [ ] `findPendentes` retorna lista limitada por `limit` ordenada por `criadoEm ASC`
