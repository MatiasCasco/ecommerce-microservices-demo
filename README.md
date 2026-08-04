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

```
docs/architecture/overview.md
```

↓

Comprender el flujo completo del negocio.

```
docs/architecture/flow.md
```

↓

Comprender la comunicación mediante eventos.

```
docs/architecture/rabbitmq.md
```

↓

Comprender el modelo de eventos.

```
docs/architecture/event-model.md
```

↓

Comprender la observabilidad distribuida.

```
docs/architecture/distributed-tracing.md
```

↓

Comprender el estándar de logging.

```
docs/architecture/logging.md
```

↓

Comprender la estrategia de seguridad.

```
docs/architecture/security.md
```

↓

Comprender la infraestructura compartida.

```
docs/architecture/common-library.md
```

↓

Comprender el modelo de proyecciones.

```
docs/architecture/projections.md
```

---

## 2. Servicios

Una vez comprendida la arquitectura, estudiar cada dominio.

```
docs/services/user-service/
```

↓

```
docs/services/product-service/
```

↓

```
docs/services/order-service/
```

---

## 3. Decisiones Arquitectónicas

Comprender por qué se diseñó el sistema de esta manera.

```
docs/decisions/
```

---

## 4. Patrones

Revisar los patrones implementados dentro del proyecto.

```
docs/patterns/
```

---

## 5. Dev Notes

Consultar únicamente durante el desarrollo.

```
docs/dev-notes/
```

Contiene contexto de implementación, decisiones temporales y notas de trabajo.
