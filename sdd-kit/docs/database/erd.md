# Banco de Dados — ERD

## Diagrama Entidade-Relacionamento

```mermaid
erDiagram
    USUARIO {
        bigserial id PK
        varchar nome
        varchar email UK
        varchar senha_hash
        varchar role
        boolean ativo
        timestamp criado_em
        timestamp atualizado_em
    }

    FILME {
        bigserial id PK
        varchar titulo
        text sinopse
        int duracao_min
        varchar genero
        varchar classificacao
        varchar poster_url
        date data_lancamento
        boolean ativo
    }

    SALA {
        bigserial id PK
        varchar nome UK
        int capacidade
        varchar tipo
        boolean ativa
    }

    ASSENTO {
        bigserial id PK
        bigint sala_id FK
        char fileira
        int numero
        varchar tipo
    }

    SESSAO {
        bigserial id PK
        bigint filme_id FK
        bigint sala_id FK
        timestamp data_hora
        varchar idioma
        varchar formato
        decimal preco
        varchar status
    }

    SESSAO_ASSENTO {
        bigserial id PK
        bigint sessao_id FK
        bigint assento_id FK
        varchar status
        timestamp reservado_ate
        bigint usuario_id FK
    }

    INGRESSO {
        bigserial id PK
        varchar codigo UK
        bigint usuario_id FK
        bigint sessao_assento_id FK
        decimal valor_pago
        varchar status
        timestamp comprado_em
    }

    PAGAMENTO {
        bigserial id PK
        bigint ingresso_id FK
        varchar transacao_id
        decimal valor
        varchar metodo
        varchar status
        timestamp processado_em
    }

    OUTBOX_EVENTS {
        bigserial id PK
        varchar event_type
        varchar aggregate_id
        jsonb payload
        varchar status
        int tentativas
        timestamp criado_em
        timestamp processado_em
    }

    USUARIO ||--o{ INGRESSO : "compra"
    USUARIO ||--o{ SESSAO_ASSENTO : "reserva"
    FILME ||--o{ SESSAO : "exibido em"
    SALA ||--o{ SESSAO : "ocorre em"
    SALA ||--o{ ASSENTO : "contém"
    SESSAO ||--o{ SESSAO_ASSENTO : "disponibiliza"
    ASSENTO ||--o{ SESSAO_ASSENTO : "participa"
    SESSAO_ASSENTO ||--o| INGRESSO : "gera"
    INGRESSO ||--o| PAGAMENTO : "possui"
```

---

## Relacionamentos — resumo

| Tabela A | Tabela B | Tipo | Chave |
|---|---|---|---|
| `usuario` | `ingresso` | 1:N | `ingresso.usuario_id` |
| `usuario` | `sessao_assento` | 1:N | `sessao_assento.usuario_id` (nullable) |
| `filme` | `sessao` | 1:N | `sessao.filme_id` |
| `sala` | `sessao` | 1:N | `sessao.sala_id` |
| `sala` | `assento` | 1:N | `assento.sala_id` |
| `sessao` | `sessao_assento` | 1:N | `sessao_assento.sessao_id` |
| `assento` | `sessao_assento` | 1:N | `sessao_assento.assento_id` |
| `sessao_assento` | `ingresso` | 1:0..1 | `ingresso.sessao_assento_id` |
| `ingresso` | `pagamento` | 1:0..1 | `pagamento.ingresso_id` |
| `outbox_events` | (independente) | — | `aggregate_id` referencia ID de outras tabelas por convenção |

---

## Notas de design

- `sessao_assento` é a tabela de junção N:N entre `sessao` e `assento`,
  mas com **estado** (status, reservado_ate, usuario_id). Não é uma simples tabela de pivot.
- `outbox_events` é desacoplada — não tem FK para `ingresso`.
  O `aggregate_id` é uma String (ex: "42") que referencia o ID do agregado por convenção de aplicação.
  Isso permite usar o Outbox com qualquer tipo de agregado sem alterar o schema.
- `reservado_ate` em `sessao_assento` é o TTL da reserva temporária.
  O controle de expiração real é feito no Redis (`reserva:{sessaoId}:{assentoId}`).
  O campo no banco serve apenas para auditoria e limpeza periódica pelo scheduler.
