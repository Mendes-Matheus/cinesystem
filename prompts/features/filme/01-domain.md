---
context:
  - docs/architecture/clean-architecture.md
  - docs/architecture/layer-rules.md
  - docs/features/filme.md
  - docs/conventions/patterns.md
---

# Tarefa: Implementar domain/filme

Implemente todos os arquivos do pacote `com.cinesystem.domain.filme`
conforme especificado em `docs/features/filme.md`.

## Arquivos a criar

### FilmeId.java
- `record FilmeId(Long valor)`
- Compact constructor: rejeita null com `DomainException`

### Genero.java
- `enum` com valores: `ACAO, COMEDIA, DRAMA, FICCAO, TERROR, ROMANCE, ANIMACAO, DOCUMENTARIO`

### ClassificacaoEtaria.java
- `record ClassificacaoEtaria(String codigo)`
- Compact constructor: aceita apenas `"L", "10", "12", "14", "16", "18"`
  → `DomainException("Classificação inválida: " + codigo)` para qualquer outro valor
- Método estático de fábrica: `public static ClassificacaoEtaria of(String codigo)`

### Filme.java
- POJO puro — zero imports de framework
- Campos conforme `docs/features/filme.md` (seção Domínio)
- Construtor público valida todas as invariantes listadas
- Método `desativar()` conforme especificado
- Getters para todos os campos (sem setters públicos)
- `toString()`, `equals()`, `hashCode()` baseados em `id`

### FilmeRepository.java
- Interface com os 4 métodos especificados em `docs/features/filme.md`

## Checklist de validação

Após gerar, verifique:
- [ ] Nenhum arquivo em `domain/filme/` importa `org.springframework.*` ou `jakarta.*`
- [ ] `Filme("")` lança `DomainException`
- [ ] `Filme` com `duracaoMinutos = 0` lança `DomainException`
- [ ] `ClassificacaoEtaria.of("99")` lança `DomainException`
- [ ] `FilmeRepository` é interface sem nenhuma anotação
