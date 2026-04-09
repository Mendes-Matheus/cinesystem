---
context:
  - docs/database/tables.md
  - docs/features/ingresso.md
---

# Tarefa: Criar migration V4 e seed de dados de desenvolvimento

## V5__seed_dev_data.sql

Crie `src/main/resources/db/migration/V5__seed_dev_data.sql` com dados
de desenvolvimento para facilitar testes manuais:

```sql
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
    sala_id BIGINT;
    fileira CHAR(1);
    num     INTEGER;
BEGIN
    SELECT id INTO sala_id FROM sala WHERE nome = 'Sala 1' LIMIT 1;
    FOREACH fileira IN ARRAY ARRAY['A','B','C','D','E','F'] LOOP
        FOR num IN 1..20 LOOP
            INSERT INTO assento (sala_id, fileira, numero, tipo)
            VALUES (sala_id, fileira, num, 'STANDARD')
            ON CONFLICT (sala_id, fileira, numero) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;
```

---

## Verificação pós-migration

Após aplicar as migrations, confirme via psql ou DBeaver:

```sql
-- Deve retornar 2 usuários
SELECT id, nome, email, role FROM usuario;

-- Deve retornar 3 filmes
SELECT id, titulo, genero FROM filme WHERE ativo = true;

-- Deve retornar 3 salas
SELECT id, nome, tipo, capacidade FROM sala;

-- Deve retornar 120 assentos para Sala 1
SELECT COUNT(*) FROM assento a JOIN sala s ON a.sala_id = s.id WHERE s.nome = 'Sala 1';
```

---

## Checklist

- [ ] V4 usa `ON CONFLICT DO NOTHING` — idempotente (pode rodar várias vezes)
- [ ] Hashes BCrypt são placeholders — documentar que devem ser trocados em produção
- [ ] Script DO $$ gera assentos dinamicamente — não duplica se rodar novamente
- [ ] Arquivo nomeado `V5__seed_dev_data.sql` (dois underscores entre versão e descrição)
