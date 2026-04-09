# Arquitetura — Clean Architecture

## Os quatro anéis

```
┌─────────────────────────────────────────────────┐
│  interfaces/          (Controllers, Schedulers)  │
│  ┌───────────────────────────────────────────┐   │
│  │  infrastructure/  (JPA, Redis, Mail)      │   │
│  │  ┌─────────────────────────────────────┐  │   │
│  │  │  application/  (Use Cases, Ports)   │  │   │
│  │  │  ┌───────────────────────────────┐  │  │   │
│  │  │  │  domain/  (Entities, VOs)     │  │  │   │
│  │  │  └───────────────────────────────┘  │  │   │
│  │  └─────────────────────────────────────┘  │   │
│  └───────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

## Regra de dependência — inviolável

```
domain  ←  application  ←  interfaces
                ↑
          infrastructure
```

Nenhuma camada interna importa nada de uma camada externa.

## Responsabilidades

| Camada           | Pode importar          | Nunca importa                        |
|------------------|------------------------|--------------------------------------|
| `domain/`        | Nada externo           | Spring, JPA, Redis, qualquer lib     |
| `application/`   | `domain/` apenas       | JPA, Redis, Spring MVC               |
| `infrastructure/`| `domain/`, `application/` ports | `interfaces/`               |
| `interfaces/`    | `application/` use cases | `infrastructure/` direto           |

## Trade-offs aceitos (Clean Architecture pragmática)

A camada `application/` usa três elementos do Spring de forma intencional:

- `@Service` — marcador IoC, sem impacto em lógica
- `@Transactional` — alternativa exigiria proxy manual ou delegar ao controller (antipadrão)
- `ApplicationEventPublisher` — apenas para eventos não-transacionais (ex: FilmeCriado)
  Para eventos transacionais (compra de ingresso), usar Outbox Pattern obrigatoriamente.

O domínio (`domain/`) permanece 100% livre de framework. Esse é o limite que não se negocia.
