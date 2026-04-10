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
