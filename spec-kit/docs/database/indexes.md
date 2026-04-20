# Banco de Dados — Índices

## Índices por tabela

### filme
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| `idx_filme_genero` | `genero` | B-tree | Filtro por gênero na listagem pública |
| `idx_filme_ativo` | `ativo` | Parcial `WHERE ativo = true` | Listagem só busca filmes ativos |
| `idx_filme_lancamento` | `data_lancamento` | B-tree | Ordenação por data de lançamento |

### sessao
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| `idx_sessao_filme_data` | `(filme_id, data_hora)` | Composto | Busca de sessões por filme ordenada por horário |
| `idx_sessao_status` | `status` | Parcial `WHERE status = 'ATIVA'` | Listagem só busca sessões ativas |

### assento
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| `idx_assento_sala` | `sala_id` | B-tree | Busca de todos os assentos de uma sala |

### sessao_assento
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| `idx_sa_sessao_status` | `(sessao_id, status)` | Composto | Contagem de disponíveis por sessão |
| `idx_sa_reservado_ate` | `reservado_ate` | Parcial `WHERE status = 'RESERVADO'` | Scheduler de limpeza de reservas expiradas |

### ingresso
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| `idx_ingresso_usuario` | `usuario_id` | B-tree | Histórico de compras do usuário |
| `idx_ingresso_codigo` | `codigo` | B-tree (UNIQUE) | Validação de QR Code na catraca |

### usuario
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| (UNIQUE email) | `email` | Já criado pela constraint UNIQUE | Busca por e-mail no login |

### outbox_events
| Índice | Coluna(s) | Tipo | Justificativa |
|--------|-----------|------|---------------|
| `idx_outbox_pendente` | `criado_em ASC` | Parcial `WHERE status = 'PENDENTE'` | Scheduler processa eventos na ordem de chegada |

---

## Queries que se beneficiam de cada índice

```sql
-- idx_sessao_filme_data
SELECT * FROM sessao
WHERE filme_id = $1 AND status = 'ATIVA'
ORDER BY data_hora ASC;

-- idx_sa_sessao_status (usado na subquery do relatório)
SELECT COUNT(*) FROM sessao_assento
WHERE sessao_id = $1 AND status = 'DISPONIVEL';

-- idx_sa_reservado_ate (scheduler de limpeza)
SELECT * FROM sessao_assento
WHERE status = 'RESERVADO' AND reservado_ate < NOW();

-- idx_outbox_pendente (OutboxProcessorScheduler)
SELECT * FROM outbox_events
WHERE status = 'PENDENTE'
ORDER BY criado_em ASC
LIMIT 50;
```

---

## Manutenção

- Monitore índices com `pg_stat_user_indexes` — remova os com `idx_scan = 0` após 30 dias
- Considere `VACUUM ANALYZE` semanal nas tabelas `sessao_assento` e `outbox_events` (alta taxa de update)
- Em produção com volume > 1M ingressos, avaliar particionamento da tabela `ingresso` por `comprado_em`
