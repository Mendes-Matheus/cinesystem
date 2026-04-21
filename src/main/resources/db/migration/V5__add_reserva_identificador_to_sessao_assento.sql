-- Adiciona a coluna para suportar o identificador de reserva (visitantes ou usuários)
ALTER TABLE sessao_assento
    ADD COLUMN reserva_identificador VARCHAR(255);

-- Cria um índice para otimizar a busca por essa coluna durante o checkout
CREATE INDEX idx_sa_reserva_id ON sessao_assento(reserva_identificador);