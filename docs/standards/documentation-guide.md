# Documentation Guide

Esta guía explica **cómo leer y navegar la documentación oficial del e-commerce**.

La documentación está pensada como un **libro técnico del proyecto**: primero se entiende el ecosistema completo y después se profundiza en cada microservicio.

> `docs/dev-notes/` es material auxiliar de desarrollo y conservación de contexto. **No forma parte del libro oficial.**

---

# 1. Cómo está organizado el libro

La documentación oficial está organizada en cinco grandes áreas:

```text
docs/
│
├── README.md
├── architecture/
├── services/
├── decisions/
├── patterns/
└── standards/
```

Cada sección responde una pregunta diferente:

| Sección | Pregunta |
|---|---|
| `README.md` | ¿Qué es el proyecto y dónde está su documentación? |
| `architecture/` | ¿Cómo funciona el ecosistema completo? |
| `services/` | ¿Cómo está diseñado cada microservicio? |
| `decisions/` | ¿Qué decisiones arquitectónicas afectan al sistema completo? |
| `patterns/` | ¿Qué patrones utilizamos o planeamos utilizar? |
| `standards/` | ¿Cómo debemos leer y mantener esta documentación? |

---

# 2. Orden recomendado de lectura

Si es la primera vez que trabajas con el proyecto, sigue este orden:

```text
1. Architecture
       ↓
2. Services
       ↓
3. Architecture Decisions
       ↓
4. Patterns
       ↓
5. Future / Roadmaps de cada servicio
```

La idea es evitar estudiar un microservicio aislado sin conocer primero el contexto arquitectónico que lo rodea.

---

# 3. Parte I — Arquitectura

La carpeta:

```text
docs/architecture/
```

explica el sistema desde una perspectiva transversal.

## 3.1 Architecture Overview

```text
docs/architecture/overview.md
```

Comenzar aquí.

Explica:

- arquitectura general;
- microservicios;
- responsabilidades;
- límites;
- dependencias;
- principios de desacoplamiento.

---

## 3.2 System Flow

```text
docs/architecture/flow.md
```

Después comprender el flujo general del ecosistema.

Permite visualizar cómo participan los servicios en las operaciones principales.

---

## 3.3 RabbitMQ

```text
docs/architecture/rabbitmq.md
```

Explica la comunicación asíncrona mediante RabbitMQ:

```text
Producer
   ↓
Exchange
   ↓
Routing Key
   ↓
Queue
   ↓
Consumer
```

Este documento debe leerse antes de estudiar los flujos event-driven específicos de los servicios.

---

## 3.4 Event Model

```text
docs/architecture/event-model.md
```

Explica cómo se representan los eventos dentro del ecosistema:

- estructura;
- metadata;
- aggregate;
- event type;
- version;
- traceId;
- timestamp;
- evolución de contratos.

---

## 3.5 Projections

```text
docs/architecture/projections.md
```

Explica el concepto de proyecciones locales y consistencia eventual.

Un ejemplo importante dentro del proyecto es:

```text
Product Service
      ↓
 Product Events
      ↓
Order Service
      ↓
ProductCatalog
```

---

## 3.6 Distributed Tracing

```text
docs/architecture/distributed-tracing.md
```

Explica la propagación de `TraceId` entre servicios y cómo se utiliza para observabilidad distribuida.

---

## 3.7 Logging

```text
docs/architecture/logging.md
```

Explica el estándar de logging del proyecto y el uso de logging estructurado.

---

## 3.8 Security

```text
docs/architecture/security.md
```

Explica la estrategia de seguridad transversal:

```text
Authentication
       ↓
Authorization
       ↓
Ownership
```

Los detalles específicos de cada servicio se estudian posteriormente dentro de cada capítulo de servicio.

---

## 3.9 Common Library

```text
docs/architecture/common-library.md
```

Explica qué responsabilidades son compartidas mediante `common-lib` y por qué existe esta infraestructura común.

La documentación específica del módulo se encuentra posteriormente en:

```text
docs/services/common-lib/
```

---

# 4. Parte II — Servicios

Una vez comprendida la arquitectura global, estudiar los servicios.

Actualmente la documentación de servicios está organizada en:

```text
docs/services/
├── common-lib/
├── user-service/
├── product-service/
└── order-service/
```

La secuencia recomendada es:

```text
User Service
     ↓
Product Service
     ↓
Order Service
```

---

# 5. User Service

Ruta:

```text
docs/services/user-service/
```

User Service es responsable de la identidad y autenticación del ecosistema.

La documentación debe estudiarse siguiendo su README y posteriormente sus documentos especializados.

Conceptualmente:

```text
User Service
    │
    ├── Identity
    ├── Authentication
    ├── Authorization
    └── JWT
```

La documentación de decisiones del servicio explica, entre otras cosas:

- User Service como Identity Provider;
- JWT;
- Stateless Authentication;
- Spring Security;
- BCrypt;
- RBAC;
- confianza de los demás servicios en el JWT;
- evolución futura hacia OAuth2, Refresh Tokens, MFA e Identity Federation.

---

# 6. Product Service

Ruta:

```text
docs/services/product-service/
```

Product Service es el dueño del catálogo de productos.

Conceptualmente:

```text
Product Service
      │
      ├── Product
      ├── Catalog
      ├── Stock
      └── Product Events
```

Una vez comprendido su dominio, estudiar:

- API;
- flujo de producto;
- publicación de eventos;
- sincronización;
- decisiones;
- roadmap;
- evolución futura.

Una decisión fundamental es:

```text
Product Service
       ↓
Source of Truth
```

Otros servicios pueden mantener proyecciones locales mediante eventos.

---

# 7. Order Service

Ruta:

```text
docs/services/order-service/
```

Order Service es el servicio con mayor profundidad de diseño actualmente.

La lectura recomendada es:

```text
README
  ↓
domain
  ↓
use-case
  ↓
api
  ↓
order-flow
  ↓
security-authorization
  ↓
product-catalog
  ↓
event-consumption
  ↓
synchronization
  ↓
decisions
  ↓
roadmap
  ↓
future
```

## 7.1 Domain

Explica:

- `Order` como Aggregate Root;
- `OrderItem`;
- `OrderStatus`;
- invariantes;
- snapshots;
- cálculo de subtotales;
- cálculo y persistencia del total;
- State Machine.

---

## 7.2 Use Case

Explica la coordinación de Application:

```text
Actor
  ↓
Authorization / Ownership
  ↓
Idempotency
  ↓
Normalize Items
  ↓
ProductCatalog
  ↓
Order
  ↓
Persistence
```

---

## 7.3 API

Explica los contratos HTTP:

```text
POST /orders
GET /orders
GET /orders/{id}
PATCH /orders/{id}/cancel
```

También define los DTOs y las reglas de acceso.

---

## 7.4 Order Flow

Explica el flujo completo de creación de una Order.

Una decisión importante es:

```text
productId = 10, quantity = 2
productId = 10, quantity = 3
              ↓
          quantity = 5
```

Los productos duplicados se consolidan antes de crear los `OrderItem`.

---

## 7.5 Product Catalog

Explica:

```text
Product Service
      ↓
Product Events
      ↓
RabbitMQ
      ↓
Order Service
      ↓
ProductCatalog
```

`ProductCatalog` es una proyección local y eventualmente consistente.

---

## 7.6 Security / Authorization

Explica la separación:

```text
Actor
  ≠
Customer
```

y:

```text
Authentication
      ↓
Authorization
      ↓
Ownership
```

---

## 7.7 Event Consumption

Explica cómo Order Service consume eventos de Product Service y mantiene `ProductCatalog`.

---

## 7.8 Synchronization

Explica la consistencia eventual entre Product Service y Order Service.

---

## 7.9 Decisions

Registra las decisiones específicas de Order Service.

Entre las decisiones ya cerradas están:

- Order como Aggregate Root;
- `Order.id` incremental gestionado por DB;
- `Order.total` calculado por Order y persistido;
- productos duplicados consolidados;
- stock validado pero no reservado;
- precio como snapshot;
- `customerId` como ownership;
- `Idempotency-Key` para `POST /orders`;
- State Machine;
- desacoplamiento de Product Service;
- evolución futura hacia Inventory, Payment, Pricing / Offer y otros patrones.

---

## 7.10 Roadmap

Explica:

```text
Diseño
  ↓
Application / Security
  ↓
API
  ↓
Create Order
  ↓
Queries
  ↓
Lifecycle
  ↓
Events
  ↓
Future Capabilities
```

---

## 7.11 Future

Explica las capacidades que no pertenecen al MVP actual, por ejemplo:

- Inventory / Reservation;
- Payment;
- Pricing / Offer;
- Order Events;
- Retry;
- DLQ;
- Publisher Confirms;
- Consumer Idempotency;
- Outbox;
- Saga;
- Customer Projection;
- observabilidad avanzada.

---

# 8. Common Library

Ruta:

```text
docs/services/common-lib/
```

La documentación arquitectónica de `common-lib` se encuentra en:

```text
docs/architecture/common-library.md
```

La documentación específica del módulo debe explicar qué contiene y cómo utilizarlo.

La separación conceptual es:

```text
architecture/common-library.md
        ↓
por qué existe y qué responsabilidad arquitectónica tiene

services/common-lib/
        ↓
qué contiene y cómo está implementado
```

---

# 9. Decisiones arquitectónicas globales

Ruta:

```text
docs/decisions/
```

Aquí se documentan decisiones que afectan al ecosistema completo.

Actualmente existen ADRs relacionados con la arquitectura event-driven.

La diferencia es:

```text
docs/decisions/
        ↓
decisiones transversales

docs/services/<service>/decisions.md
        ↓
decisiones específicas del servicio
```

No duplicar una decisión específica de un servicio en los ADR globales salvo que tenga impacto transversal.

---

# 10. Patterns

Ruta:

```text
docs/patterns/
```

Contiene los patrones utilizados o contemplados por el proyecto.

Los patrones deben distinguir entre:

```text
Patrones actualmente utilizados
```

y:

```text
Patrones futuros
```

No presentar una capacidad futura como si ya estuviera implementada.

Ejemplos futuros del ecosistema:

```text
Outbox
DLQ
Publisher Confirms
Consumer Idempotency
Saga
```

---

# 11. Standards

Esta carpeta contiene las reglas para mantener la documentación.

```text
docs/standards/
```

La propia guía que estás leyendo pertenece aquí.

Los estándares deben explicar:

- propósito de cada tipo de documento;
- convenciones de nombres;
- nivel de detalle;
- navegación;
- cuándo crear una decisión;
- cómo documentar capacidades futuras;
- cómo mantener consistencia entre documentos.

---

# 12. Dev Notes

Existe una carpeta auxiliar:

```text
docs/dev-notes/
```

Su propósito es exclusivamente conservar:

- contexto de chats;
- contexto de implementación;
- decisiones temporales;
- material de trabajo;
- información útil para continuar sesiones.

### Importante

`dev-notes` **no forma parte de la documentación oficial del libro**.

No debe utilizarse como fuente principal para entender la arquitectura.

Cuando el contenido de una nota de trabajo se convierte en una decisión definitiva, debe trasladarse al documento oficial correspondiente.

Ejemplo:

```text
dev-notes/
    ↓
decisión definitiva
    ↓
decisions.md / ADR / domain.md / api.md
```

Cuando ya no sea necesario conservar estas notas, la carpeta puede eliminarse.

---

# 13. Cómo debe evolucionar la documentación

La documentación sigue el mismo principio que la arquitectura:

```text
Problema
   ↓
Decisión
   ↓
Implementación
   ↓
Tests
   ↓
Documentación actualizada
```

Si durante la implementación aparece una contradicción real:

```text
Código / restricción técnica
          ↓
Identificar contradicción
          ↓
Revisar decisión
          ↓
Actualizar documentación afectada
          ↓
Implementar
```

No cambiar silenciosamente una decisión arquitectónica solo para adaptar la documentación al código.

---

# 14. Regla de consistencia

Una misma decisión debe tener una única definición conceptual.

Puede aparecer referenciada en varios documentos, pero no debe tener diferentes significados.

Ejemplo:

```text
Order.total
```

debe significar lo mismo en:

```text
domain.md
use-case.md
api.md
order-flow.md
decisions.md
README.md
```

Los documentos pueden explicar la decisión desde perspectivas diferentes, pero no contradecirse.

---

# 15. Cómo leer un microservicio

Cada servicio puede tener una estructura diferente según sus responsabilidades, pero generalmente:

```text
README
   ↓
Domain
   ↓
Use Cases / Application
   ↓
API
   ↓
Flows
   ↓
Integration / Events
   ↓
Security
   ↓
Decisions
   ↓
Roadmap
   ↓
Future
```

No todos los servicios necesitan todos los documentos.

La documentación debe existir cuando una responsabilidad o decisión justifique documentarla.

---

# 16. Estado actual del libro

Actualmente:

```text
Architecture
    🟢 Base sólida

User Service
    🟢 Documentación estructurada

Product Service
    🟢 Documentación estructurada

Order Service
    🟢 Diseño muy avanzado y revisado

Common Library
    🟢 Documentación existente

Patterns
    🟢 Base existente

Decisions
    🟢 Base existente

Standards
    🟢 Esta guía

Dev Notes
    🟡 Material auxiliar, fuera del libro
```

El objetivo no es crear documentación por cantidad.

El objetivo es que un desarrollador pueda entrar al repositorio y recorrerlo como un libro técnico coherente.

---

# 17. Regla final

La documentación oficial debe responder progresivamente:

```text
¿Qué estamos construyendo?
        ↓
¿Cómo está construido?
        ↓
¿Por qué está construido así?
        ↓
¿Cómo funciona cada servicio?
        ↓
¿Qué decisiones tomó cada servicio?
        ↓
¿Qué está implementado?
        ↓
¿Qué puede evolucionar en el futuro?
```

Si un documento no ayuda a responder alguna de estas preguntas, evaluar si realmente debe formar parte del libro.
