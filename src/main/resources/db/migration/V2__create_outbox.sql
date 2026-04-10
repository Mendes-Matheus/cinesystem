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
