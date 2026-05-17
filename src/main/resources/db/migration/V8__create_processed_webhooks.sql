-- ============================================================
-- V8: Tabela de deduplicação operacional de webhooks processados
-- ============================================================
-- Propósito: idempotência atômica via INSERT ON CONFLICT.
-- Esta tabela é LEAN (sem payload/headers) para evitar
-- crescimento descontrolado. Auditoria completa fica em
-- webhook_audit_log (Phase 2).
--
-- Constraint principal: UNIQUE (payment_id, status_processado)
--   → Garante que a combinação paymentId+status só seja
--     processada uma vez, mesmo sob concorrência.
--
-- O INSERT é feito DENTRO da mesma @Transactional do domínio:
--   Se o domínio sofrer rollback, este registro some junto.
--   Sem janela de inconsistência.
-- ============================================================

CREATE TABLE IF NOT EXISTS processed_webhooks (
    id                BIGSERIAL    PRIMARY KEY,
    payment_id        VARCHAR(50)  NOT NULL,
    status_processado VARCHAR(30)  NOT NULL,
    notification_id   VARCHAR(50),       -- apenas para tracing/log, não é idempotency key
    processado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_processed_webhook
        UNIQUE (payment_id, status_processado)
);

-- Para busca rápida por notificationId (suporte/troubleshooting)
CREATE INDEX idx_processed_webhooks_notification_id
    ON processed_webhooks(notification_id)
    WHERE notification_id IS NOT NULL;

-- Para auditoria de todos os webhooks de um mesmo pagamento
CREATE INDEX idx_processed_webhooks_payment_id
    ON processed_webhooks(payment_id);
