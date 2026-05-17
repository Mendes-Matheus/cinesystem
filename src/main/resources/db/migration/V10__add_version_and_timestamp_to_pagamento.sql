-- ============================================================
-- V10: Suporte a @Version (optimistic locking futuro)
--      e monotonicidade temporal de eventos MP
-- ============================================================
-- version:
--   Campo @Version do JPA. Incrementado automaticamente em
--   cada UPDATE. Protege contra lost updates fora do fluxo
--   lockado (ex: admin panel, migrations manuais).
--   Viabiliza optimistic locking se pessimistic se mostrar
--   inadequado para o volume futuro.
--
-- mp_ultima_atualizacao:
--   Timestamp do campo date_last_updated retornado pela API
--   do MP na consulta server-to-server. Usado para detectar
--   eventos fora de ordem: se o evento novo é mais antigo
--   que o último processado, logar WARN mas não rejeitar.
-- ============================================================

ALTER TABLE pagamento
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE pagamento
    ADD COLUMN IF NOT EXISTS mp_ultima_atualizacao TIMESTAMP;
