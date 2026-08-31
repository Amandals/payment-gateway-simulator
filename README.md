# Payment Gateway Simulator

Payment Gateway Simulator é uma aplicação backend desenvolvida em Java 17 e Spring Boot com foco em simular o processamento de pagamentos e demonstrar conceitos de arquitetura, persistência, validação, processamento assíncrono e resiliência.

O projeto está sendo desenvolvido de forma incremental, adicionando cada tecnologia conforme existe uma necessidade arquitetural real.

---

## Tecnologias

### Atualmente implementadas

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Bean Validation
* Lombok
* MapStruct
* Maven
* Docker
* JUnit 5
* Mockito
* AssertJ
* Testcontainers
* Swagger/OpenAPI

### Conceitos implementados
* Idempotência
* Controle de concorrência
* State transitions
* Transações

### Planejadas

* Outbox Pattern
* RabbitMQ
* Retry
* Circuit Breaker
* Dead Letter Queue (DLQ)
* Webhooks
* Actuator
* Logs estruturados
* Arquitetura preparada para AWS

---

## Arquitetura

O projeto utiliza uma separação por responsabilidades:

```text
Controller
     ↓
Facade
     ├── Mapper
     │
     └── Context
            ↓
          Service
            ↓
        Repository 
            ↓
        PostgreSQL
```

### Responsabilidades

**Controller**

Responsável pela camada HTTP, recebendo as requisições e retornando as respostas da API.

**Facade**

Orquestra o fluxo da operação e atua como ponto de entrada da aplicação.

**Mapper**

Responsável pela conversão entre DTOs e entidades utilizando MapStruct.

**Context**

Responsável pelas regras de negócio e decisões relacionadas ao domínio do pagamento.

**Service**

Responsável pelas operações relacionadas à persistência e acesso aos dados através do Repository.

**Repository**

Responsável pelo acesso ao banco de dados utilizando Spring Data JPA.

---

## Payment

O pagamento possui atualmente os seguintes atributos:

```text
id
amount
currency
status
description
idempotencyKey
createdAt
updatedAt
```

### PaymentStatus

Os estados do pagamento são representados por um enum e persistidos como texto no PostgreSQL:
```text
PENDING
AUTHORIZED
DECLINED
CANCELLED
REFUNDED
```

A utilização de `EnumType.STRING` evita que a alteração da ordem dos valores do enum altere o significado dos dados já persistidos.
Transições de estado

As operações disponíveis atualmente respeitam regras de transição de estado.

Autorizar
PENDING → AUTHORIZED

Um pagamento que já esteja autorizado, cancelado, recusado ou reembolsado não pode ser autorizado novamente.

Cancelar
PENDING → CANCELLED
AUTHORIZED → CANCELLED
Recusar
PENDING → DECLINED
Reembolsar
AUTHORIZED → REFUNDED

Transições inválidas são rejeitadas pelas regras de negócio.

Exemplo:
```text
DECLINED → AUTHORIZED ❌
CANCELLED → REFUNDED ❌
REFUNDED → REFUNDED ❌
```

## Datas

Os timestamps utilizam `Instant`:

```java
private Instant createdAt;
private Instant updatedAt;
```

A aplicação utiliza UTC como referência através de `Clock`:

```java
Clock.systemUTC();
```

Essa abordagem evita dependência do timezone da máquina em que a aplicação está executando e facilita testes determinísticos.

---

## Identificação

Os pagamentos utilizam UUID como identificador.

A geração é realizada pelo Hibernate:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

A aplicação não precisa gerar manualmente o identificador durante a criação do pagamento.

---

## Validação

A API utiliza Bean Validation nos DTOs.

Exemplo:

```java
@NotNull(message = "Amount is required")
@DecimalMin(
        value = "0.01",
        message = "Amount must be greater than zero"
)
private BigDecimal amount;
```
Também são utilizadas validações para:
campos obrigatórios;
valores mínimos;
tamanho dos campos;
formato da moeda.
As validações de entrada são executadas antes da requisição chegar às regras de negócio.

---

## Fluxo de criação

Atualmente o fluxo de criação de um pagamento é:

```text
POST /payments
      ↓
PaymentController
      ↓
PaymentRequest
      ↓
Bean Validation
      ↓
PaymentFacade
      ↓
PaymentMapper
      ↓
PaymentContext
      ↓
PaymentService
      ↓
PaymentRepository
      ↓
PostgreSQL
```

Durante a criação, o Context define:

* status inicial como `PENDING`
* `createdAt`
* `updatedAt`

O UUID é gerado pelo Hibernate durante a persistência.

---

## Banco de dados

O banco utilizado atualmente é PostgreSQL.

As alterações de schema são controladas pelo Flyway.

Migration inicial:

```text
V1__create_payment_table.sql
V2__add_idempotency_key_to_payment.sql
```

O objetivo é manter o schema versionado e reproduzível entre ambientes.

---

## Roadmap

### BASE
*[x] Spring Boot
*[x] Maven
*[x] Docker Compose
*[x] PostgreSQL
*[x] Flyway
*[x] Migration
*[x] Payment Entity
*[x] PaymentStatus
*[x] Repository

### API
*[x] DTO
*[x] Bean Validation
*[x] MapStruct
*[x] Service
*[x] Facade
*[x] Context
*[x] Controller
*[x] Exception Handler
*[x] Swagger/OpenAPI

### DOMÍNIO
*[x] State transitions
*[x] Timestamps com Clock
*[x] UUID
*[x] Idempotency-Key
*[x] UNIQUE constraint
*[x] Idempotência básica
*[x] Concorrência
*[ ] Auditoria

### ASSÍNCRONO
*[x] Outbox Pattern
*[x] Outbox Publisher
*[ ] RabbitMQ
*[ ] Payment Consumer
*[ ] Mock Payment Provider


Arquitetura Planejada: 
  ┌────────────── PostgreSQL ──────────────┐
  │ Payment       +       OutboxEvent      │
  └──────────────────┬─────────────────────┘
                     │
                     ▼
                 Publisher
                     │
                     ▼
                  RabbitMQ

### RESILIÊNCIA
*[ ] Retry
*[ ] Circuit Breaker
*[ ] Dead Letter Queue
*[ ] Webhook

### OBSERVABILIDADE
*[ ] Actuator
*[ ] Logs estruturados

### QUALIDADE
*[x] JUnit 5
*[x] Mockito
*[x] AssertJ
*[x] Service tests
*[x] Facade tests
*[x] Context tests
*[x] Controller integration tests
*[x] Idempotency tests
*[x] Concurrency tests
*[ ] Async processing tests
*[ ] Retry tests
*[ ] DLQ tests

### INFRAESTRUTURA
*[ ] Docker Compose final
*[x] Swagger/OpenAPI
*[ ] Documentação completa
*[ ] Diagrama de arquitetura
*[ ] Cenário de demonstração
*[ ] Arquitetura AWS 