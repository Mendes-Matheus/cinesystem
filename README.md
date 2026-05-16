# AmenicSystem

API para gestão de cinemas, permitindo o gerenciamento de filmes, sessões, reservas de assentos e venda de ingressos com integração real de pagamentos.

Aplicação desenvolvida utilizando Java e Spring Boot, aplicando conceitos como Clean Architecture, DDD, CQRS, Transactional Outbox Pattern e controle de concorrência distribuído com Redis.

O projeto simula cenários reais de alta concorrência durante o processo de compra de ingressos, garantindo integridade nas reservas de assentos e comunicação assíncrona confiável entre os componentes do sistema.

---

## 🚀 Tecnologias Utilizadas

* **Linguagem:** Java 21.
* **Framework:** Spring Boot 4.
* **Base de Dados:** PostgreSQL.
* **Cache e Locks Distribuídos:** Redis.
* **Mercado Pago SDK:** Processamento de pagamentos
* **Ngrok:** Exposição local para Webhooks e integrações externas.
* **Migrations:** Flyway.
* **Segurança:** Spring Security & JWT.
* **Testes:** JUnit, Mockito e **Testcontainers**.
* **Documentação:** Swagger/OpenAPI.
* **Containerização:** Docker & Docker Compose.

---
## 🏗️ Arquitetura e Padrões

O sistema utiliza uma abordagem híbrida com **Clean Architecture**, **DDD (Domain-Driven Design)** e **Arquitetura Hexagonal (Ports & Adapters)**, garantindo que as regras de negócio (Domínio) sejam completamente independentes de tecnologias externas.


### Camadas:

1. **Domain:** Contém as entidades de negócio, *Value Objects* e as regras puras.
2. **Application:** Implementa os casos de uso e define as interfaces para comunicação externa.
3. **Infrastructure:** Contém as implementações técnicas como repositórios JPA, integração com Redis e Gateways de pagamento.
4. **Interfaces:** Camada de entrada, gerindo controladores REST e agendadores.

### Padrões de Destaque

* **Transactional Outbox Pattern:** Garante que eventos (como o envio de e-mail após a compra) sejam processados de forma fiável, mesmo em caso de falha de rede.
* **Distributed Locks:** Utiliza o Redis para garantir que um assento não seja reservado por dois utilizadores simultaneamente durante o processo de checkout.
* **CQRS (Leitura/Escrita):** Separação de interfaces para persistência e consultas otimizadas através de projeções DTO.
* **Testcontainers:** Testes de integração utilizando ambientes reais e isolados.

---

## 🚀 Principais Funcionalidades

- **Venda de Ingressos:** Fluxo completo desde a escolha do assento até a confirmação via Webhook.
- **Integração Mercado Pago:** Pagamentos via Checkout Pro (Pix e Cartão) com tratamento de status.
- **Controle de Concorrência:** Uso de **Distributed Locks com Redis** para evitar que dois usuários reservem o mesmo assento no mesmo milissegundo.
- **Transactional Outbox:** Garantia de que eventos colaterais (como envio de e-mails) ocorram apenas se a transação no banco de dados for bem-sucedida.
- **Reserva Temporária:** Assentos ficam bloqueados durante o checkout e liberados automaticamente em caso de desistência ou falha no pagamento.

---

## 🛠️ Como Executar

### Pré-requisitos

* Docker e Docker Compose.
* JDK 21+.
* Maven.
* Ngrok.

### Passo a Passo

1. **Configurar o Ambiente:**
   O projeto utiliza variáveis de ambiente configuradas no `application.yml`. Certifique-se de que os serviços PostgreSQL e Redis estão ativos.

   Crie um arquivo `.env` na raiz ou configure o `application.yml`:

- `MERCADOPAGO_ACCESS_TOKEN_DEV`: Seu token do Mercado Pago.
- `MERCADOPAGO_NOTIFICATION_URL_DEV`: URL para receber Webhooks (ex: via Ngrok).
- `MERCADOPAGO_BACK_URL_SUCCESS_DEV`: URL de sucesso do Mercado Pago.
- `MERCADOPAGO_BACK_URL_PENDING_DEV`: URL de pendência do Mercado Pago.
- `MERCADOPAGO_BACK_URL_FAIL_DEV`: URL de falha do Mercado Pago.
- `DB_URL_DEV`: URL do banco de dados.
- `DB_USER_DEV`: Usuário do banco de dados.
- `DB_PASS_DEV`: Senha do banco de dados.
- `REDIS_HOST_DEV`: Host do Redis.
- `REDIS_PORT_DEV`: Porta do Redis.
- `JWT_SECRET_DEV`: Segredo do JWT.

2. **Iniciar a API:**
   
   Executar via Docker Compose
   ```bash
   docker-compose up -d
   ```
   ou
   ```bash
   mvn spring-boot:run
   ```
   
3. Expor a porta da API com Ngrok
   Se a API estiver rodando na porta 8081:
   ```bash
   ngrok http 8081
   ```
   O Ngrok irá gerar uma URL pública semelhante a: `https://abcd-1234.ngrok-free.app`
   
   Substitua a variável de ambiente `MERCADOPAGO_NOTIFICATION_URL_DEV` pela URL gerada pelo Ngrok com o path `/api/v1/payments/webhook` no final.

4. **Acessar a Documentação:**
   Após iniciar, aceda a: `http://localhost:8081/swagger-ui.html` para explorar os endpoints.

---
## 🧪 Testes

O projeto possui testes unitários e de integração focados na confiabilidade das regras de negócio e infraestrutura.

Para executar os testes:

```bash
mvn test
```

Tipos de testes
- Unitários: Regras de domínio e casos de uso.
- Integração: Persistência, Redis e fluxos completos utilizando Testcontainers.

---

Desenvolvido com ☕ por **Matheus Mendes**.