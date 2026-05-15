# AmenicSystem

API para gestão completa de cinemas, permitindo o gerenciamento de filmes, sessões, reservas de assentos e venda de ingressos.

Aplicação desenvolvida utilizando Java e Spring Boot, aplicando conceitos modernos de engenharia de software como Clean Architecture, DDD, CQRS, Transactional Outbox Pattern e controle de concorrência distribuído com Redis.

O projeto simula cenários reais de alta concorrência durante o processo de compra de ingressos, garantindo integridade nas reservas de assentos e comunicação assíncrona confiável entre os componentes do sistema.

## 🚀 Tecnologias Utilizadas

* **Linguagem:** Java 21.
* **Framework:** Spring Boot 4.
* **Base de Dados:** PostgreSQL.
* **Cache e Locks Distribuídos:** Redis.
* **Migrations:** Flyway.
* **Segurança:** Spring Security & JWT.
* **Testes:** JUnit, Mockito e **Testcontainers**.
* **Documentação:** Swagger/OpenAPI.
* **Containerização:** Docker & Docker Compose.

## 🏗️ Arquitetura e Padrões

O projeto segue os princípios de **Clean Architecture** e **DDD (Domain-Driven Design)**, estruturado nas seguintes camadas:

1. **Domain:** Contém as entidades de negócio, *Value Objects* e as regras puras (ex: `Sessao`, `Ingresso`, `Filme`).
2. **Application:** Implementa os casos de uso (`Use Cases`) e define as interfaces (`Ports`) para comunicação externa.
3. **Infrastructure:** Contém as implementações técnicas (`Adapters`), como repositórios JPA, integração com Redis e Gateways de pagamento.
4. **Interfaces:** Camada de entrada, gerindo controladores REST e agendadores (`Schedulers`).

### Padrões de Destaque

* **Transactional Outbox Pattern:** Garante que eventos (como o envio de e-mail após a compra) sejam processados de forma fiável, mesmo em caso de falha de rede.
* **Distributed Locks:** Utiliza o Redis para garantir que um assento não seja reservado por dois utilizadores simultaneamente durante o processo de checkout.
* **CQRS (Leitura/Escrita):** Separação de interfaces para persistência e consultas otimizadas através de projeções DTO.
* **Testcontainers:** Testes de integração utilizando ambientes reais e isolados.

## 🛠️ Como Executar

### Pré-requisitos

* Docker e Docker Compose.
* JDK 21+.
* Maven.

### Passo a Passo

1. **Configurar o Ambiente:**
   O projeto utiliza variáveis de ambiente configuradas no `application.yml`. Certifique-se de que os serviços PostgreSQL e Redis estão ativos.

2. **Executar via Docker Compose:**
   
   ```bash
   docker-compose up -d
   ```
   
   Isto subirá a API, o Postgres e o Redis automaticamente.

3. **Aceder à Documentação:**
   Após iniciar, aceda a: `http://localhost:8081/swagger-ui.html` para explorar os endpoints.

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

Desenvolvido por Matheus Mendes.
