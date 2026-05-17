-- ============================================================
-- V9: Correlação canônica payment_id no Pagamento
-- ============================================================
-- Problema resolvido:
--   O MP usa dois IDs distintos:
--     - preferenceId (ex: APP_USR-abc123) → transacao_externa_id
--     - paymentId (ex: 1405334703) → campo novo payment_id
--
--   O webhook envia o paymentId. A busca primária deve ser
--   por este campo. externalReference (ingresso-{id}) deixa
--   de ser o mecanismo de correlação.
--
-- Estratégia de migração:
--   Pagamentos antigos terão payment_id = NULL inicialmente.
--   Na primeira notificação recebida, o campo é vinculado
--   via UPDATE WHERE payment_id IS NULL (idempotente).
-- ============================================================

ALTER TABLE pagamento ADD COLUMN IF NOT EXISTS payment_id VARCHAR(50);

-- Índice único parcial: apenas registros com payment_id preenchido
-- Evita conflito com NULLs de pagamentos antigos não migrados
CREATE UNIQUE INDEX IF NOT EXISTS idx_pagamento_payment_id
    ON pagamento(payment_id)
    WHERE payment_id IS NOT NULL;
