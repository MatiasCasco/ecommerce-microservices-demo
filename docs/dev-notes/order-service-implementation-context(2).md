# Order Service — Implementation Context / Handoff

> **Purpose:** This document is the compact context to upload into a new ChatGPT conversation so implementation of `order-service` can continue without rebuilding the architectural discussion from scratch.
>
> **Source:** The design documents provided for `order-service` (`README.md`, `domain.md`, `use-cases.md`, `api.md`, `order-flow.md`, `product-catalog.md`, `synchronization.md`, `event-consumption.md`, `security-authorization.md`, `decisions.md`, `roadmap.md`, `future.md`), plus the implementation progress already completed in the current conversation.

---

# 1. Project Context

Ecommerce microservices project:

- Java 21
- Spring Boot 3.3.5
- Maven
- PostgreSQL
- RabbitMQ
- Spring Security / JWT
- Log4j2
- `common-lib`

Services:

```text
user-service
product-service
order-service
notification-service
api-gateway
common-lib
```

Current architectural principle:

> **Design for evolution, but implement only what has real business/architectural weight now.**

Avoid introducing patterns, infrastructure, abstractions, or distributed mechanisms merely because they are common in microservice architectures.

---

# 2. Current State

## Product Catalog Projection

The local `ProductCatalog` projection inside `order-service` has already been migrated from a layered architecture to Hexagonal Architecture.

Current structure:

```text
order-service
|
├── domain
│   ├── model
│   │   └── ProductCatalog.java
│   └── enums
│       └── ProductStatus.java
|
├── application
│   ├── port
│   │   ├── in
│   │   │   └── ProductCatalogSynchronizationUseCase.java
│   │   └── out
│   │       └── ProductCatalogRepository.java
│   └── service
│       └── ProductCatalogSynchronizationService.java
|
├── adapter
│   ├── in
│   │   └── consumer
│   │       └── ProductCatalogConsumer.java
│   └── out
│       └── persistence
│           ├── entity
│           │   └── ProductCatalogEntity.java
│           ├── ProductCatalogJpaRepository.java
│           └── ProductCatalogPersistenceAdapter.java
|
└── config
    └── RabbitMQConfig.java
```

The domain `ProductCatalog` is now independent of JPA.

The persistence adapter maps:

```text
ProductCatalog domain
        ↕
ProductCatalogEntity
```

RabbitMQ consumption goes through the input port.

The five existing Product events remain unchanged:

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_ACTIVATED
PRODUCT_DEACTIVATED
PRODUCT_STOCK_UPDATED
```

The five existing queues remain unchanged.

Do NOT redesign these queues as part of the Order implementation.

## Observability Already Improved

RabbitMQ consumer processing propagates the event `traceId` into Log4j2 MDC using:

```java
private void executeWithTraceId(String traceId, Runnable action) {
    try {
        MDC.put(TraceConstants.TRACE_ID, traceId);
        action.run();
    } finally {
        MDC.remove(TraceConstants.TRACE_ID);
    }
}
```

Successful synchronization also logs:

```text
PRODUCT_CATALOG_SYNCHRONIZED
```

with correlation information such as:

```text
eventType
eventId
aggregateId
traceId
```

This was validated for Product events.

The existing duplicated `CommerceLog` block is intentionally not being refactored merely for aesthetics.

---

# 3. ProductCatalog Architectural Role

`Product Service` is the **Source of Truth** for the catalog.

`Order Service` maintains a local projection:

```text
Product Service
      │
      │ Product Events
      ▼
   RabbitMQ
      │
      ▼
ProductCatalog
      │
      ▼
 Order Service
```

The projection is eventually consistent.

During Order creation:

```text
Order Service
      │
      ▼
ProductCatalog
```

There is **no REST call to Product Service** to retrieve product data.

`ProductCatalog` supplies the currently known:

```text
productId
productName
price
availableStock
status
```

Stock validation against this projection does **not** reserve stock.

Important:

```text
Order CREATED
      ≠
Stock RESERVED
```

Inventory / Reservation is a future responsibility.

---

# 4. Order Domain — Definitive Design

## 4.1 Aggregate

`Order` is the **Aggregate Root**.

```text
Order
│
├── id
├── customerId
│
├── OrderItem[]
│    ├── productId
│    ├── productName
│    ├── unitPrice
│    ├── quantity
│    └── subtotal
│
├── total
├── status
├── createdAt
└── updatedAt
```

`OrderItem` belongs to the `Order` aggregate and should not be manipulated independently.

## 4.2 Historical Snapshot

`OrderItem` stores the commercial information used at purchase time.

It preserves:

```text
productId
productName
unitPrice
quantity
subtotal
```

Example:

```text
ProductCatalog at purchase time
        │
        ▼
     OrderItem
        │
        ▼
 historical Order
```

If the product changes later, the existing Order does not change.

The Order must not be reconstructed from the current ProductCatalog.

---

# 5. Order Invariants

## Order

An Order must:

1. Contain at least one `OrderItem`.
2. Not contain the same `productId` more than once.
3. Have `total == sum(OrderItem.subtotal)`.
4. Keep commercial content immutable after creation.
5. Start in `PENDING_PAYMENT`.
6. Allow `PENDING_PAYMENT -> PAID`.
7. Allow `PENDING_PAYMENT -> CANCELLED`.
8. Treat `PAID` as terminal in the MVP.
9. Treat `CANCELLED` as terminal in the MVP.
10. Never be physically deleted.

Commercial fields that become immutable:

```text
productId
productName
unitPrice
quantity
subtotal
total
customerId
```

`createdAt` is immutable.

`updatedAt` changes when the resource is meaningfully updated.

`Order.id` is incremental and generated by the database. The generation strategy belongs to persistence, not domain logic.

## OrderItem

Each item must satisfy:

```text
quantity > 0
unitPrice > 0
subtotal = unitPrice × quantity
```

`productName` and `unitPrice` represent the historical snapshot.

---

# 6. Order State Machine

MVP states:

```text
PENDING_PAYMENT
PAID
CANCELLED
```

Transitions:

```text
                 ┌──────────┐
                 │          ▼
        ┌──────────────────────────┐
        │     PENDING_PAYMENT      │
        └────────────┬─────────────┘
                     │
              ┌──────┴──────┐
              │             │
           pay()         cancel()
              │             │
              ▼             ▼
        ┌──────────┐   ┌───────────┐
        │   PAID   │   │ CANCELLED │
        └──────────┘   └───────────┘
          terminal       terminal
```

Aggregate behavior:

```java
order.pay();
order.cancel();
```

Do NOT allow arbitrary state changes through a public setter.

Important:

`PAID` exists in the model, but payment is not implemented in the MVP. A future Payment Service will confirm payment and cause the transition.

An ADMIN must not manually set an Order to `PAID`.

---

# 7. Create Order — Main Use Case

## Endpoint

```http
POST /orders
Idempotency-Key: <unique-key>
```

The `Idempotency-Key` is mandatory.

## Request

One HTTP DTO is used for USER and ADMIN:

```text
CreateOrderRequest
├── customerId?
└── items[]
    └── CreateOrderItemRequest
        ├── productId
        └── quantity
```

The client does NOT provide:

```text
unitPrice
subtotal
total
status
createdAt
updatedAt
```

Prices come from `ProductCatalog`.

---

# 8. Actor / Customer Rules

Important conceptual separation:

```text
Actor ≠ Customer
```

The actor executes the operation.

`customerId` identifies the customer who owns the Order.

## USER

For a USER:

```text
customerId = authenticatedCustomerId
```

A USER must not be able to use a request `customerId` to create an Order for another customer.

## ADMIN

For an ADMIN:

```text
customerId = request.customerId
```

when the authorization rules permit it.

The domain must not know:

```text
JWT
Spring Security
SecurityContext
ROLE_USER
ROLE_ADMIN
```

Authentication, authorization, and ownership belong outside the domain, coordinated by Application.

---

# 9. Create Order Flow

The definitive conceptual flow is:

```text
Client
  │
  ▼
POST /orders
  │
  ▼
Idempotency-Key
  │
  ▼
Authentication / Actor
  │
  ▼
Authorization / Ownership
  │
  ▼
Consolidate duplicate products
  │
  ▼
ProductCatalog
  │
  ├── product exists
  ├── status = ACTIVE
  └── availableStock >= requested quantity
  │
  ▼
Build OrderItems
  │
  ▼
Build Order Aggregate
  │
  ├── calculate subtotals
  ├── calculate total
  └── PENDING_PAYMENT
  │
  ▼
Persist
  ├── Order
  ├── OrderItems
  └── IdempotencyRecord
  │
  ▼
COMMIT
  │
  ▼
HTTP 201
  │
  ▼
OrderResponse
```

Order creation must be a single local transaction:

```text
BEGIN
   Order
   OrderItems
   IdempotencyRecord
COMMIT
```

On failure:

```text
ROLLBACK
```

No Order should remain persisted without the required idempotency record, or vice versa.

---

# 10. Duplicate Product Consolidation

Duplicate products in the request are NOT an error.

Example:

```text
productId=10 quantity=2
productId=10 quantity=3
```

becomes:

```text
productId=10 quantity=5
```

This normalization happens in Application before constructing the Aggregate.

Result:

```text
Order
 └── at most one OrderItem per productId
```

The domain protects the invariant; Application performs request normalization.

---

# 11. Product Validation

For each normalized product:

```text
product exists
       +
status == ACTIVE
       +
availableStock >= requested quantity
```

Failures are business errors such as:

```text
ProductNotFound
ProductInactive
InsufficientStock
```

Stock is only the known value in the local projection.

There is no reservation.

Do not introduce Inventory logic into the Order aggregate.

---

# 12. Pricing Rules

The client never controls pricing.

```text
ProductCatalog.price
        │
        ▼
OrderItem.unitPrice
        │
        ▼
OrderItem.subtotal
        │
        ▼
Order.total
```

Calculation:

```text
subtotal = unitPrice × quantity

total = sum(all subtotals)
```

The calculated total is persisted.

The MVP accepts eventual consistency of ProductCatalog price.

Future Pricing / Offer is explicitly out of scope.

---

# 13. API

Base path:

```text
/orders
```

Endpoints:

```text
POST  /orders
GET   /orders/{id}
GET   /orders
PATCH /orders/{id}/cancel
```

## POST /orders

Response:

```text
HTTP 201 Created
OrderResponse
```

## GET /orders/{id}

Response:

```text
OrderResponse
```

Historical information comes from the stored Order.

No Product Service REST call.

## GET /orders

Response:

```text
Page<OrderSummaryResponse>
```

Summary does not contain OrderItems.

Conceptually:

```text
OrderSummaryResponse
├── id
├── customerId
├── total
├── status
├── createdAt
└── updatedAt
```

Supports:

- pagination;
- sorting;
- `customerId` filter.

USER queries must remain restricted to their own customer.

ADMIN can query according to the administrative capabilities defined for the MVP.

## PATCH /orders/{id}/cancel

The client does not send a new state.

The operation invokes:

```java
order.cancel();
```

Valid transition:

```text
PENDING_PAYMENT -> CANCELLED
```

Invalid transitions must be rejected by the Aggregate.

---

# 14. Response DTOs

## OrderResponse

```text
OrderResponse
├── id
├── customerId
├── items[]
│   └── OrderItemResponse
│       ├── productId
│       ├── productName
│       ├── unitPrice
│       ├── quantity
│       └── subtotal
├── total
├── status
├── createdAt
└── updatedAt
```

## OrderSummaryResponse

```text
OrderSummaryResponse
├── id
├── customerId
├── total
├── status
├── createdAt
└── updatedAt
```

---

# 15. Idempotency — MVP Requirement

`POST /orders` requires:

```http
Idempotency-Key: <unique-key>
```

The idempotency record is stored in PostgreSQL.

Conceptually:

```text
PostgreSQL
├── orders
├── order_items
└── idempotency_records
```

## Same key + same request

Return the original result.

Do not create another Order.

```text
Request 1
   │
   ▼
Order 100

Retry
same key + same request
   │
   ▼
return Order 100
```

## Same key + different request

Reject the operation.

Conceptually use a request fingerprint/hash to determine whether the request is the same logical request.

```text
same key + same request
    → original result

same key + different request
    → error
```

## Why PostgreSQL?

Do NOT introduce Redis, Caffeine, or another external store solely for Order idempotency.

The guarantee must survive application restarts.

---

# 16. Relevant Business Errors

Conceptually:

```text
ProductNotFound
ProductInactive
InsufficientStock
OrderNotFound
OrderNotCancellable
UnauthorizedOrderAccess
InvalidOrderItem
Idempotency-Key errors
```

Duplicate products are not an error.

Important architectural separation:

```text
Business rule violation
        ≠
Infrastructure failure
```

Database/RabbitMQ/technical failures must not be artificially converted into business errors.

When implementing HTTP error handling, use the existing `common-lib` exception/error mechanism consistently with the rest of the project.

Do not prematurely create infrastructure-specific error abstractions unless implementation actually requires them.

---

# 17. Hexagonal Architecture for Order

The ProductCatalog projection already uses this structure.

Order should follow the same overall organization:

```text
order-service
|
├── domain
│   ├── model
│   │   ├── Order.java
│   │   └── OrderItem.java
│   └── enums
│       └── OrderStatus.java
|
├── application
│   ├── port
│   │   ├── in
│   │   │   ├── CreateOrderUseCase.java
│   │   │   ├── GetOrderUseCase.java
│   │   │   ├── ListOrdersUseCase.java
│   │   │   └── CancelOrderUseCase.java
│   │   └── out
│   │       ├── OrderRepository.java
│   │       ├── ProductCatalogRepository.java
│   │       └── IdempotencyRepository.java
│   └── service
│       ├── CreateOrderService.java
│       ├── GetOrderService.java
│       ├── ListOrdersService.java
│       └── CancelOrderService.java
|
├── adapter
│   ├── in
│   │   └── web
│   │       ├── controller
│   │       └── dto
│   └── out
│       └── persistence
│           ├── entity
│           ├── repository
│           └── adapter
|
└── config
```

This is the target organization, not a command to create every file immediately.

Build incrementally.

---

# 18. Responsibilities by Layer

## Domain

Responsible for:

```text
Order
OrderItem
OrderStatus
invariants
calculations
state transitions
```

Domain must NOT know:

```text
Spring
JPA
RabbitMQ
REST
JWT
SecurityContext
Controllers
Repositories
API Gateway
```

## Application

Responsible for coordination:

```text
Actor
Authorization / ownership
customerId resolution
request normalization
duplicate consolidation
ProductCatalog lookup
product validation
stock validation
Aggregate creation
transaction coordination
idempotency coordination
repository orchestration
```

Application must not duplicate Aggregate business rules.

## Adapters In

HTTP controller is responsible for translating:

```text
HTTP
  ↓
Application input
```

Do not put domain logic in controllers.

## Adapters Out

Persistence adapters translate:

```text
Domain
  ↕
Persistence Entity
```

JPA must remain outside the domain.

---

# 19. Recommended Implementation Order

Do NOT implement the entire service in one step.

Use incremental slices.

## Step 1 — Domain

First implement:

```text
OrderStatus
OrderItem
Order
```

Validate:

- item quantity;
- positive price;
- subtotal calculation;
- Order total;
- at least one item;
- initial `PENDING_PAYMENT`;
- `cancel()`;
- `pay()`;
- invalid transitions;
- immutable commercial state.

Write domain unit tests before infrastructure.

## Step 2 — Application Contract

Define the input model for Create Order:

```text
CreateOrderCommand
├── customerId
├── items[]
│   ├── productId
│   └── quantity
└── actor
```

Keep HTTP DTOs separate from the domain.

Define the input port:

```text
CreateOrderUseCase
```

Define required output ports.

Do not add ports without a real consumer/dependency.

## Step 3 — Create Order Application Service

Implement the orchestration:

```text
Idempotency
   ↓
Actor / ownership
   ↓
Normalize items
   ↓
ProductCatalog
   ↓
Validate products
   ↓
Validate stock
   ↓
Build OrderItems
   ↓
Build Order
   ↓
Persist atomically
```

## Step 4 — Persistence

Introduce:

```text
OrderEntity
OrderItemEntity
IdempotencyRecordEntity
```

and the required JPA repositories/adapters.

Domain remains JPA-free.

## Step 5 — Transaction

Guarantee:

```text
Order
+
OrderItems
+
IdempotencyRecord
```

are persisted atomically.

## Step 6 — API

Implement:

```text
POST /orders
```

with:

- request validation;
- `Idempotency-Key`;
- actor resolution;
- response mapping;
- centralized error handling.

## Step 7 — Integration Tests

Test the complete flow:

```text
HTTP
 ↓
Application
 ↓
ProductCatalog
 ↓
Domain
 ↓
Persistence
```

Focus especially on idempotency.

---

# 20. After Create Order

Once Create Order is stable:

## Queries

Implement:

```text
GET /orders/{id}
GET /orders
```

with:

- ownership;
- ADMIN capabilities;
- pagination;
- sorting;
- summary projection.

## Lifecycle

Implement:

```text
PATCH /orders/{id}/cancel
```

and State Machine tests.

`PAID` remains future behavior until Payment Service exists.

---

# 21. Explicitly Out of Scope for MVP

Do NOT implement these now unless a concrete requirement appears:

```text
Payment Service
Inventory Service
Stock Reservation
Pricing / Offer
Notification integration
Order Events
Retry
DLQ
Publisher Confirms
Consumer Idempotency
Outbox Pattern
Saga Pattern
Idempotency end-to-end
Customer Projection
Distributed tracing infrastructure
Advanced observability
```

Important distinction:

```text
HTTP Idempotency
POST /orders
        ↓
MVP

Consumer Idempotency
messages/events
        ↓
Future
```

Likewise:

```text
Order creation
      ≠
inventory reservation
```

and:

```text
Order created
      ≠
payment confirmed
```

---

# 22. Future Architecture

Eventually:

```text
                    ┌───────────────┐
                    │ Order Service │
                    └───────┬───────┘
                            │
                      ORDER_CREATED
                            │
                         RabbitMQ
                    ┌───────┼────────┐
                    ▼       ▼        ▼
                Payment  Inventory  Notification
```

Potential future lifecycle:

```text
PENDING_PAYMENT
      │
      │ PaymentConfirmedEvent
      ▼
     PAID
```

Potential Inventory evolution:

```text
reserve
confirm
release
```

Potential distributed coordination:

```text
Create Order
     ↓
Reserve Stock
     ↓
Process Payment
     ↓
Compensating actions if needed
```

Potential Outbox:

```text
Transaction
├── Order
└── Outbox Event
       ↓
    Publisher
       ↓
    RabbitMQ
```

These are future evolutions, not current implementation requirements.

---

# 23. Architectural Rules to Preserve

1. **Domain drives implementation.**
2. **Order is the Aggregate Root.**
3. **OrderItem is a historical snapshot.**
4. **Order protects its invariants.**
5. **Application coordinates; Domain decides domain validity.**
6. **Authorization is not a domain responsibility.**
7. **Actor ≠ Customer.**
8. **Product Service owns the catalog.**
9. **ProductCatalog is a local projection.**
10. **No REST Product Service call during Create Order.**
11. **Stock validation is not stock reservation.**
12. **Price is obtained from ProductCatalog and stored as a snapshot.**
13. **Order total is calculated by Order.**
14. **Order content is immutable after creation.**
15. **State changes happen through Aggregate behavior, not arbitrary setters.**
16. **POST /orders is idempotent using PostgreSQL-backed `Idempotency-Key`.**
17. **Order + OrderItems + IdempotencyRecord are one transaction.**
18. **Do not introduce future infrastructure prematurely.**
19. **Keep adapters outside the domain.**
20. **Implement incrementally and test each meaningful architectural boundary.**

---

# 24. Implementation Philosophy for the New Chat

The new implementation conversation should work **file-by-file and decision-by-decision**.

Preferred workflow:

```text
Understand requirement
        ↓
Identify architectural responsibility
        ↓
Choose layer
        ↓
Implement smallest correct change
        ↓
Explain why
        ↓
Test
        ↓
Review
        ↓
Continue
```

Do not jump directly to generating the whole service.

When a design choice is ambiguous:

```text
Problem
  ↓
Options
  ↓
Trade-offs
  ↓
Choose the simplest option that satisfies the current requirement
```

Do not add abstractions merely to make the architecture "look more hexagonal".

---

# 25. First Concrete Task in the New Chat

Start here:

```text
Create the Order domain model.
```

First files:

```text
domain/model/Order.java
domain/model/OrderItem.java
domain/enums/OrderStatus.java
```

Before coding, verify the exact current `order-service` source tree and existing dependencies so the implementation fits the real project.

Then implement and test:

```text
OrderItem
 ├── validation
 └── subtotal

Order
 ├── construction
 ├── item management
 ├── total calculation
 ├── PENDING_PAYMENT
 ├── cancel()
 └── pay()
```

Do not start with Controller, JPA, RabbitMQ, or future services.

The first goal is:

> **Have a correct, framework-independent Order Aggregate before connecting it to Application and infrastructure.**

---

# 26. Definition of Done for the Current Development Phase

Create Order is not considered complete until:

```text
[x] Domain model
[x] Domain invariants
[x] State machine
[x] Duplicate consolidation
[x] ProductCatalog integration
[x] Product validation
[x] Stock validation
[x] Order + OrderItems persistence
[x] IdempotencyRecord persistence
[x] Atomic transaction
[x] HTTP API
[x] Error handling
[x] Domain tests
[x] Use case tests
[x] Integration tests
[x] Same-key/same-request retry test
[x] Same-key/different-request rejection test
```

After that, move to:

```text
Get Order
   ↓
List Orders
   ↓
Cancel Order
   ↓
Lifecycle tests
```

---

# 27. Important Final Constraint

The existing ProductCatalog synchronization has already been treated as completed work.

The next major objective is **not another architectural migration**.

It is:

```text
                    ORDER SERVICE
                         │
                         ▼
                 Order Aggregate
                         │
                ┌────────┴────────┐
                ▼                 ▼
          Create Order       Order lifecycle
                │
                ▼
          Idempotency
                │
                ▼
          Persistence
                │
                ▼
               API
```

The project is now moving from:

```text
architecture/design of Order Service
```

to:

```text
incremental implementation of the designed Order Service
```

Keep the previously defined design as the source of truth unless a real implementation constraint forces a documented decision change.
