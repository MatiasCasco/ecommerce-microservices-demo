# Product Service Roadmap

## Objetivo

Este documento describe la evolución planificada de **Product Service**.

El servicio constituye la fuente oficial del catálogo de productos dentro del ecosistema y continuará evolucionando para ofrecer mayor confiabilidad, escalabilidad y resiliencia.

---

# Estado Actual

## Dominio

- [x] Gestión de Productos.
- [x] Gestión de Categorías.
- [x] Activación y desactivación de productos.
- [x] Administración de precios.
- [x] Administración de stock.

---

## API

- [x] CRUD de Productos.
- [x] CRUD de Categorías.
- [x] Specification API.
- [x] Paginación.
- [x] Ordenamiento.
- [x] Filtros dinámicos.

---

## Seguridad

- [x] JWT Authentication.
- [x] Autorización basada en Roles.
- [x] Protección de endpoints administrativos.

---

## Event-Driven

- [x] Publicación de eventos de dominio.
- [x] RabbitMQ Producer.
- [x] Eventos compartidos mediante common-lib.

Eventos implementados:

- [x] PRODUCT_CREATED
- [x] PRODUCT_UPDATED
- [x] PRODUCT_ACTIVATED
- [x] PRODUCT_DEACTIVATED
- [x] PRODUCT_STOCK_UPDATED

---

## Observabilidad

- [x] Logging estructurado.
- [x] TraceId distribuido.
- [x] Manejo centralizado de errores.

---

# Próxima Evolución

## Confiabilidad

- [ ] Publisher Confirms.
- [ ] Retry de publicación.
- [ ] Dead Letter Queue (DLQ).
- [ ] Manejo avanzado de errores de mensajería.

Objetivo:

Garantizar que ningún evento del dominio se pierda durante la publicación.

---

## Consistencia

- [ ] Outbox Pattern.
- [ ] Publicación transaccional de eventos.
- [ ] Idempotencia.

Objetivo:

Asegurar que los cambios en la base de datos y la publicación de eventos permanezcan sincronizados.

---

## Observabilidad

- [ ] OpenTelemetry.
- [ ] Métricas de publicación.
- [ ] Trazabilidad distribuida.
- [ ] Dashboards.
- [ ] Alertas.

Objetivo:

Facilitar el monitoreo y diagnóstico del servicio.

---

## Rendimiento

- [ ] Optimización de consultas.
- [ ] Caché para lecturas frecuentes.
- [ ] Optimización de índices.
- [ ] Ajuste de paginación.

Objetivo:

Mantener un buen rendimiento a medida que el catálogo crezca.

---

## Auditoría

- [ ] Historial de cambios.
- [ ] Auditoría de productos.
- [ ] Auditoría de stock.
- [ ] Auditoría de precios.

Objetivo:

Permitir reconstruir el historial de modificaciones del catálogo.

---

# Integraciones Futuras

Los eventos publicados por Product Service podrán ser consumidos por nuevos servicios.

Servicios previstos:

- [ ] Order Service
- [ ] Inventory Service
- [ ] Notification Service
- [ ] Search Service
- [ ] Recommendation Service
- [ ] Analytics Service

La incorporación de nuevos consumidores no requerirá modificaciones en Product Service.

---

# Evolución Arquitectónica

El servicio continuará evolucionando siguiendo los principios definidos para el proyecto.

Próximas mejoras arquitectónicas:

- [ ] Event Versioning.
- [ ] Schema Evolution.
- [ ] Contract Testing.
- [ ] Resiliencia en mensajería.
- [ ] Escalabilidad horizontal.
- [ ] Arquitectura Cloud Native.

---

# Estado de Madurez

| Área | Estado |
|-------|--------|
| Dominio | ✅ Maduro |
| API REST | ✅ Maduro |
| Seguridad | ✅ Maduro |
| RabbitMQ | ✅ Funcional |
| Event-Driven | ✅ Primera versión |
| Resiliencia | 🚧 En evolución |
| Observabilidad | 🚧 En evolución |
| Arquitectura Distribuida | 📅 Futuro |

---

# Visión

Product Service continuará siendo la **fuente oficial del catálogo** dentro del ecosistema.

Su evolución estará orientada a fortalecer la confiabilidad en la publicación de eventos, mejorar la observabilidad y soportar un número creciente de consumidores sin incrementar el acoplamiento entre microservicios.