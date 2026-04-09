---
context:
  - docs/architecture/layer-rules.md
  - docs/features/ingresso.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar OutboxProcessorScheduler

Implemente o scheduler que processa eventos pendentes da tabela `outbox_events`.
A classe fica em `interfaces/scheduler/OutboxProcessorScheduler.java`.

## Contrato

```java
@Component
@RequiredArgsConstructor
public class OutboxProcessorScheduler {

    private final OutboxRepository outboxRepository;
    private final EmailPort        emailPort;         // porta de saída — não JavaMailSender direto
    private final ObjectMapper     objectMapper;

    @Scheduled(fixedDelay = 5_000)   // a cada 5 segundos
    @Transactional
    public void processar() { ... }

    private void despachar(OutboxEvent evento) { ... }
}
```

## Comportamento de processar()

1. `outboxRepository.findPendentes(50)` — batch de até 50 por ciclo
2. Para cada evento:
   - Chama `despachar(evento)` dentro de try/catch
   - Sucesso: `evento.marcarProcessado()`
   - Falha: `evento.registrarFalha(ex.getMessage())`
   - Sempre: `outboxRepository.save(evento)`
3. Não lança exceção — falhas são absorvidas e registradas no próprio evento

## Comportamento de despachar()

- Faz switch por `evento.getEventType()`
- `"IngressoComprado"`:
  1. Deserializa payload: `objectMapper.readValue(evento.getPayload(), IngressoCompradoPayload.class)`
  2. Chama `emailPort.enviarConfirmacaoIngresso(payload)`
- `default`: loga warning e chama `evento.registrarFalha("Tipo de evento desconhecido")`

## EmailPort (porta — já deve existir)

```java
public interface EmailPort {
    void enviarConfirmacaoIngresso(IngressoCompradoPayload payload);
}
```

Confirme que a interface existe em `application/port/out/`.
Se não existir, crie-a antes de implementar o scheduler.

## Regras importantes

- O scheduler está em `interfaces/scheduler/` — pode injetar portas e repositórios da `application/`
- Não injeta `JavaMailSender`, `RedisTemplate` ou qualquer classe de `infrastructure/` diretamente
- `@Transactional` garante que o update de status do evento é atômico

## Checklist

- [ ] `@Scheduled(fixedDelay = 5_000)` presente
- [ ] Try/catch absorve falhas — nunca deixa o scheduler quebrar
- [ ] `outboxRepository.save(evento)` chamado tanto em sucesso quanto em falha
- [ ] Deserialização usa `ObjectMapper`, não parsing manual
- [ ] Não injeta `JavaMailSender` diretamente
