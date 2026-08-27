# 🛒 E-commerce Microservices

## 🏗️ Architecture

Ver documentación completa en:

👉 /docs

---

## 📦 Services

- user-service
- product-service
- order-service
- notification-service
- api-gateway

---

## 🐳 Run

```bash
docker-compose up --build
```

# Documentation Guide

Si es la primera vez que trabajas con este proyecto, se recomienda leer la documentación en el siguiente orden.

## 1. Arquitectura

Comprender la arquitectura general del ecosistema.

```text
docs/architecture/overview.md
```

↓

Comprender el flujo completo del negocio.

```text
docs/architecture/flow.md
```

↓

Comprender la comunicación mediante eventos.

```text
docs/architecture/rabbitmq.md
```

↓

Comprender el modelo de eventos.

```text
docs/architecture/event-model.md
```

↓

Comprender el modelo de proyecciones y consistencia eventual.

```text
docs/architecture/projections.md
```

↓

Comprender la observabilidad distribuida.

```text
docs/architecture/distributed-tracing.md
```

↓

Comprender el estándar de logging.

```text
docs/architecture/logging.md
```

↓

Comprender la estrategia de seguridad.

```text
docs/architecture/security.md
```

↓

Comprender la infraestructura compartida.

```text
docs/architecture/common-library.md
```

---

## 2. Servicios

Una vez comprendida la arquitectura, estudiar cada dominio.

```text
docs/services/user-service/
```

↓

```text
docs/services/product-service/
```

↓

```text
docs/services/order-service/
```

La documentación de cada servicio contiene su propio README y los documentos necesarios para comprender su dominio, casos de uso, API, flujos, decisiones y evolución.

---

## 3. Decisiones Arquitectónicas

Comprender por qué se diseñó el sistema de esta manera.

```text
docs/decisions/
```

Las decisiones globales se documentan aquí.

Las decisiones específicas de cada microservicio se encuentran dentro de la documentación de su respectivo servicio.

---

## 4. Patrones

Revisar los patrones implementados dentro del proyecto y las capacidades que se contemplan para su evolución.

```text
docs/patterns/
```

---

## 5. Standards

Consultar las convenciones utilizadas para organizar y mantener la documentación.

```text
docs/standards/documentation-guide.md
```

Esta guía explica cómo leer el conjunto de documentación como un libro técnico y qué responsabilidad tiene cada sección.

---

## 6. Dev Notes

Consultar únicamente durante el desarrollo.

```text
docs/dev-notes/
```

Contiene contexto de implementación, decisiones temporales y notas de trabajo utilizadas para conservar el contexto de los chats.

Esta carpeta no forma parte de la documentación oficial del proyecto.
