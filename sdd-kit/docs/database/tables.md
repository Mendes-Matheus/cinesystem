# Banco de Dados — Tabelas e DDL

## Convenções gerais
- Todas as PKs: `BIGSERIAL PRIMARY KEY`
- Enums: `VARCHAR` (não PostgreSQL ENUM — facilita migrations)
- Datas: `TIMESTAMP NOT NULL DEFAULT NOW()`
- FKs: sempre com `ON DELETE RESTRICT` salvo explicitação contrária

---

## V1__create_core_tables.sql

```sql
CREATE TABLE usuario (
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(120)  NOT NULL,
    email        VARCHAR(180)  NOT NULL UNIQUE,
    senha_hash   VARCHAR(255)  NOT NULL,
    role         VARCHAR(20)   NOT NULL DEFAULT 'CLIENTE',
    ativo        BOOLEAN       NOT NULL DEFAULT true,
    criado_em    TIMESTAMP     NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE filme (
    id               BIGSERIAL PRIMARY KEY,
    titulo           VARCHAR(200)  NOT NULL,
    sinopse          TEXT,
    duracao_min      INTEGER       NOT NULL CHECK (duracao_min > 0),
    genero           VARCHAR(50)   NOT NULL,
    classificacao    VARCHAR(10)   NOT NULL,
    poster_url       VARCHAR(500),
    data_lancamento  DATE          NOT NULL,
    ativo            BOOLEAN       NOT NULL DEFAULT true
);

CREATE TABLE sala (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(50)   NOT NULL UNIQUE,
    capacidade  INTEGER       NOT NULL CHECK (capacidade > 0),
    tipo        VARCHAR(20)   NOT NULL,   -- 2D | 3D | IMAX | VIP
    ativa       BOOLEAN       NOT NULL DEFAULT true
);

CREATE TABLE assento (
    id       BIGSERIAL PRIMARY KEY,
    sala_id  BIGINT       NOT NULL REFERENCES sala(id),
    fileira  CHAR(1)      NOT NULL,
    numero   INTEGER      NOT NULL,
    tipo     VARCHAR(20)  NOT NULL,  -- STANDARD | VIP | ACESSIBILIDADE
    UNIQUE (sala_id, fileira, numero)
);

CREATE TABLE sessao (
    id               BIGSERIAL PRIMARY KEY,
    filme_id         BIGINT          NOT NULL REFERENCES filme(id),
    sala_id          BIGINT          NOT NULL REFERENCES sala(id),
    data_hora        TIMESTAMP       NOT NULL,
    idioma           VARCHAR(20)     NOT NULL,
    formato          VARCHAR(10)     NOT NULL,
    preco            DECIMAL(8,2)    NOT NULL CHECK (preco > 0),
    status           VARCHAR(20)     NOT NULL DEFAULT 'ATIVA'
);

CREATE TABLE sessao_assento (
    id            BIGSERIAL PRIMARY KEY,
    sessao_id     BIGINT     NOT NULL REFERENCES sessao(id),
    assento_id    BIGINT     NOT NULL REFERENCES assento(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    reservado_ate TIMESTAMP,
    usuario_id    BIGINT     REFERENCES usuario(id),
    UNIQUE (sessao_id, assento_id)
);

CREATE TABLE ingresso (
    id                 BIGSERIAL PRIMARY KEY,
    codigo             VARCHAR(36)     NOT NULL UNIQUE,  -- UUID
    usuario_id         BIGINT          NOT NULL REFERENCES usuario(id),
    sessao_assento_id  BIGINT          NOT NULL REFERENCES sessao_assento(id),
    valor_pago         DECIMAL(8,2)    NOT NULL,
    status             VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',
    comprado_em        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE pagamento (
    id              BIGSERIAL PRIMARY KEY,
    ingresso_id     BIGINT          NOT NULL REFERENCES ingresso(id),
    transacao_id    VARCHAR(100),
    valor           DECIMAL(8,2)    NOT NULL,
    metodo          VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    processado_em   TIMESTAMP
);
```

---

## V2__create_outbox.sql

```sql
CREATE TABLE outbox_events (
    id             BIGSERIAL PRIMARY KEY,
    event_type     VARCHAR(100)  NOT NULL,
    aggregate_id   VARCHAR(100)  NOT NULL,
    payload        JSONB         NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    tentativas     INTEGER       NOT NULL DEFAULT 0,
    criado_em      TIMESTAMP     NOT NULL DEFAULT NOW(),
    processado_em  TIMESTAMP
);
```

---

## V3__create_indexes.sql

```sql
-- Filme
CREATE INDEX idx_filme_genero      ON filme(genero);
CREATE INDEX idx_filme_ativo       ON filme(ativo) WHERE ativo = true;
CREATE INDEX idx_filme_lancamento  ON filme(data_lancamento);

-- Sessão
CREATE INDEX idx_sessao_filme_data  ON sessao(filme_id, data_hora);
CREATE INDEX idx_sessao_status      ON sessao(status) WHERE status = 'ATIVA';

-- Sessão-Assento
CREATE INDEX idx_sa_sessao_status   ON sessao_assento(sessao_id, status);
CREATE INDEX idx_sa_reservado_ate   ON sessao_assento(reservado_ate)
    WHERE status = 'RESERVADO';

-- Ingresso
CREATE INDEX idx_ingresso_usuario   ON ingresso(usuario_id);
CREATE INDEX idx_ingresso_codigo    ON ingresso(codigo);

-- Outbox
CREATE INDEX idx_outbox_pendente    ON outbox_events(criado_em ASC)
    WHERE status = 'PENDENTE';
```
