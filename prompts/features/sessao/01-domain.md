---
context:
  - docs/architecture/layer-rules.md
  - docs/features/sessao.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar domain/sessao e domain/assento

## domain/assento/

### AssentoId.java
- `record AssentoId(Long valor)` — rejeita null

### TipoAssento.java
- `enum`: `STANDARD, VIP, ACESSIBILIDADE`

### StatusAssento.java
- `enum`: `DISPONIVEL, RESERVADO, OCUPADO, MANUTENCAO`

### Assento.java
- POJO puro
- Campos: `AssentoId id`, `SalaId salaId`, `String fileira`, `int numero`, `TipoAssento tipo`
- Construtor valida: fileira não nula/vazia, numero > 0

### AssentoRepository.java
- Interface:
  ```java
  List<Assento> findBySala(SalaId salaId);
  Optional<Assento> findById(AssentoId id);
  Assento save(Assento assento);
  ```

---

## domain/sessao/

### SessaoId.java, FormatoExibicao.java, StatusSessao.java
- Conforme especificação em `docs/features/sessao.md`

### SessaoAssento.java
- Campos: `id`, `sessaoId`, `assentoId`, `status`, `reservadoAte`, `usuarioId`
- Método `confirmarCompra(UsuarioId usuarioId)`:
  1. Valida `status == DISPONIVEL || (status == RESERVADO && this.usuarioId.equals(usuarioId))`
     → `DomainException("Assento não disponível para este usuário")` se falhar
  2. Muda `status` para `OCUPADO`
  3. Cria e retorna `new Ingresso(CodigoIngresso.gerar(), usuarioId, this.id, sessao.getPreco())`
     — nota: a sessão precisa ser passada como parâmetro para acessar o preço

### Sessao.java
- POJO puro
- Invariantes conforme `docs/features/sessao.md`
- Método `cancelar()`: valida status == ATIVA, muda para CANCELADA

### SessaoRepository.java
- Interface com os 4 métodos especificados

## Checklist

- [ ] `SessaoAssento.confirmarCompra()` retorna `Ingresso` (não void)
- [ ] `Sessao` e `SessaoAssento` são POJOs puros — zero framework
- [ ] `StatusSessao` tem os 4 valores: ATIVA, LOTADA, CANCELADA, ENCERRADA
