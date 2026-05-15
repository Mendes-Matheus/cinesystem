ALTER TABLE pagamento ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE pagamento ADD COLUMN IF NOT EXISTS dados_retorno TEXT;
ALTER TABLE pagamento RENAME COLUMN transacao_id TO transacao_externa_id;
