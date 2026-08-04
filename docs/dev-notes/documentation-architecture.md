# Architecture Documentation - Next Phase

## Estado Actual

Se completó la documentación de los principales módulos del proyecto siguiendo un estándar común.

### Servicios documentados

- ✅ User Service
- ✅ Product Service
- ✅ Order Service
- ✅ Common Library (README)

Cada servicio contiene:

- README.md
- domain.md
- flow.md (o authentication-flow.md)
- api.md
- roadmap.md
- future.md
- decisions.md

Además, Product Service y Order Service incluyen documentación específica de Event-Driven Architecture.

---

# Documentación General

Actualmente existe la siguiente estructura.

docs/

├── architecture/
├── decisions/
├── dev-notes/
├── patterns/
├── services/
└── standards/

Se agregó además:

```
docs/standards/documentation-guide.md
```

que define el propósito de cada documento.

---

# Filosofía adoptada

Durante la documentación se definió una nueva metodología.

No queremos escribir documentación técnica aislada.

Queremos construir un **libro de arquitectura**.

Cada documento debe responder una única pregunta y preparar naturalmente el siguiente.

---

# Narrativa de Arquitectura

El orden recomendado de lectura será:

README.md

↓

architecture/overview.md

↓

architecture/flow.md

↓

architecture/rabbitmq.md

↓

architecture/event-model.md

↓

architecture/projections.md

↓

architecture/distributed-tracing.md

↓

architecture/logging.md

↓

architecture/security.md

↓

architecture/common-library.md

↓

services/

↓

decisions/

---

# Estado de Architecture

## overview.md

Reestructurado completamente.

Ya no describe servicios.

Ahora explica:

- visión general
- dominios
- comunicación
- principios
- arquitectura del ecosistema

Este documento quedó prácticamente terminado.

---

## Próximo documento

```
architecture/flow.md
```

## Objetivo

Responder una única pregunta:

> ¿Cómo ocurre un proceso de negocio dentro del ecosistema?

No debe explicar Product Service.

No debe explicar Order Service.

No debe explicar RabbitMQ.

Debe contar una historia.

---

# Nueva metodología para escribir documentación

Cada documento debe seguir esta estructura.

## 1. Objetivo

¿Qué aprenderá el lector?

---

## 2. Historia

Explicar el problema desde el punto de vista del negocio.

No hablar todavía de Spring ni RabbitMQ.

---

## 3. Arquitectura

Mostrar qué servicios participan.

---

## 4. Comunicación

Explicar cómo interactúan.

REST

RabbitMQ

Eventos

---

## 5. Principios

¿Por qué se diseñó así?

DDD

EDA

Eventual Consistency

Single Source of Truth

Database per Service

---

## 6. Evolución

¿Cómo crecerá este componente?

---

# Próximos documentos

Después de flow.md continuaremos con:

```
rabbitmq.md
```

Este será probablemente el documento más importante del proyecto.

Debe responder:

> ¿Cómo se comunican los microservicios?

No comenzará explicando Exchanges.

Comenzará explicando:

¿Por qué REST no era suficiente?

↓

¿Por qué mensajería?

↓

¿Por qué RabbitMQ?

↓

Exchange

↓

Queue

↓

Routing Key

↓

Producer

↓

Consumer

↓

Retry

↓

DLQ

↓

Publisher Confirm

↓

Outbox

↓

Idempotencia

↓

Buenas prácticas

---

Luego continuaremos con:

- event-model.md
- projections.md
- distributed-tracing.md
- logging.md
- security.md
- common-library.md

---

# Objetivo Final

La carpeta architecture debe poder leerse como un libro.

Cada documento responde una única pregunta.

overview.md

↓

¿Cómo está construido el ecosistema?

↓

flow.md

↓

¿Cómo ocurre una operación de negocio?

↓

rabbitmq.md

↓

¿Cómo se comunican los servicios?

↓

event-model.md

↓

¿Qué información viaja entre ellos?

↓

projections.md

↓

¿Cómo mantenemos la consistencia?

↓

distributed-tracing.md

↓

¿Cómo seguimos una operación entre microservicios?

↓

logging.md

↓

¿Cómo observamos el sistema?

↓

security.md

↓

¿Cómo protegemos el ecosistema?

↓

common-library.md

↓

¿Qué infraestructura compartimos?

El objetivo no es escribir documentación.

El objetivo es construir una guía de arquitectura completa que explique el diseño del ecosistema desde el negocio hasta la implementación.