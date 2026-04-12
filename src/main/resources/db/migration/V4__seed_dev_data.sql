-- ─── Usuário Admin ───────────────────────────────────────────────────────────
-- Senha: Admin@123 (hash BCrypt custo 12 — substituir em produção via variável)
INSERT INTO usuario (nome, email, senha_hash, role, ativo)
VALUES (
    'Administrador',
    'admin@cinesystem.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J.k9AzFWG',
    'ADMIN',
    true
) ON CONFLICT (email) DO NOTHING;

-- ─── Usuário Cliente de teste ─────────────────────────────────────────────────
-- Senha: Cliente@123
INSERT INTO usuario (nome, email, senha_hash, role, ativo)
VALUES (
    'Cliente Teste',
    'cliente@cinesystem.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J.k9AzFWG',
    'CLIENTE',
    true
) ON CONFLICT (email) DO NOTHING;

-- ─── Filmes de exemplo ────────────────────────────────────────────────────────
INSERT INTO filme (titulo, sinopse, duracao_min, genero, classificacao, data_lancamento, ativo)
VALUES
    ('Duna: Parte Dois', 'Continuação da saga de Paul Atreides.', 166, 'FICCAO', '14', '2024-03-01', true),
    ('Pobres Criaturas', 'A jovem Bella Baxter é trazida de volta à vida.', 141, 'DRAMA', '18', '2024-02-08', true),
    ('Oppenheimer', 'A história do pai da bomba atômica.', 180, 'DRAMA', '14', '2023-07-20', true)
ON CONFLICT DO NOTHING;

-- ─── Salas ───────────────────────────────────────────────────────────────────
INSERT INTO sala (nome, capacidade, tipo, ativa)
VALUES
    ('Sala 1', 120, '2D', true),
    ('Sala 2', 80,  '3D', true),
    ('IMAX',   200, 'IMAX', true)
ON CONFLICT (nome) DO NOTHING;

-- ─── Assentos da Sala 1 (fileiras A-F, 20 lugares cada) ──────────────────────
DO $$
DECLARE
    v_sala_id BIGINT;
    v_fileira CHAR(1);
    v_num     INTEGER;
BEGIN
    SELECT id INTO v_sala_id FROM sala WHERE nome = 'Sala 1' LIMIT 1;
    FOREACH v_fileira IN ARRAY ARRAY['A','B','C','D','E','F'] LOOP
        FOR v_num IN 1..20 LOOP
            INSERT INTO assento (sala_id, fileira, numero, tipo)
            VALUES (v_sala_id, v_fileira, v_num, 'STANDARD')
            ON CONFLICT (sala_id, fileira, numero) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;
