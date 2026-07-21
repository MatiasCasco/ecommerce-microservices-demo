# Arquitectura

## Introducción

Esta sección reúne la documentación arquitectónica de la plataforma.

Su objetivo es explicar las decisiones de diseño, los patrones utilizados y la forma en que los distintos componentes colaboran entre sí.

La documentación está organizada de manera progresiva. Cada documento introduce nuevos conceptos que sirven de base para comprender el siguiente.

---

# Orden de Lectura

Se recomienda seguir el siguiente orden.

```
overview.md
      │
      ▼
flow.md
      │
      ▼
rabbitmq.md
      │
      ▼
event-model.md
      │
      ▼
projections.md
      │
      ▼
distributed-tracing.md
      │
      ▼
logging.md
      │
      ▼
security.md
      │
      ▼
common-library.md
```

---

# Guía de Lectura

## 1. Overview

**Archivo**

```
overview.md
```

**Pregunta que responde**

> ¿Qué es esta plataforma y cómo está organizada?

Aquí se presenta la visión general de la arquitectura, los microservicios que componen el sistema y las responsabilidades de cada uno.

---

## 2. Flujo de Negocio

**Archivo**

```
flow.md
```

**Pregunta que responde**

> ¿Cómo ocurre una operación de negocio dentro de la plataforma?

Describe el recorrido completo de una operación desde la solicitud del cliente hasta la publicación de eventos.

---

## 3. Comunicación entre Microservicios

**Archivo**

```
rabbitmq.md
```

**Pregunta que responde**

> ¿Cómo se comunican los microservicios?

Explica la arquitectura basada en eventos, el uso de RabbitMQ y la interacción entre productores, exchanges, colas y consumidores.

---

## 4. Modelo de Eventos

**Archivo**

```
event-model.md
```

**Pregunta que responde**

> ¿Qué información viaja entre los microservicios?

Describe la estructura común de todos los eventos publicados por la plataforma.

---

## 5. Proyecciones

**Archivo**

```
projections.md
```

**Pregunta que responde**

> ¿Cómo consumen los microservicios la información recibida?

Explica el concepto de proyección, la sincronización mediante eventos y el papel de `ProductCatalog` como representación local del dominio de productos.

---

## 6. Trazabilidad Distribuida

**Archivo**

```
distributed-tracing.md
```

**Pregunta que responde**

> ¿Cómo seguimos una operación entre múltiples microservicios?

Describe el uso del Trace ID, MDC y la propagación del contexto durante toda la ejecución.

---

## 7. Logging

**Archivo**

```
logging.md
```

**Pregunta que responde**

> ¿Cómo observamos el comportamiento del sistema?

Explica la estrategia de logging estructurado, la relación con la trazabilidad distribuida y el uso de `CommerceLog`.

---

## 8. Seguridad

**Archivo**

```
security.md
```

**Pregunta que responde**

> ¿Cómo protegemos los recursos de la plataforma?

Describe el proceso de autenticación con JWT, la autorización basada en roles y la integración con Spring Security.

---

## 9. Biblioteca Compartida

**Archivo**

```
common-library.md
```

**Pregunta que responde**

> ¿Cómo compartimos capacidades técnicas sin acoplar los dominios?

Explica el propósito del módulo `common-lib` y las responsabilidades compartidas entre los distintos microservicios.

---

# Mapa Conceptual

La documentación se encuentra organizada en cuatro grandes bloques arquitectónicos.

```
Arquitectura General
│
├── overview.md
└── flow.md

Comunicación
│
├── rabbitmq.md
├── event-model.md
└── projections.md

Observabilidad
│
├── distributed-tracing.md
└── logging.md

Infraestructura
│
├── security.md
└── common-library.md
```

---

# Público Objetivo

Esta documentación está orientada a:

- Desarrolladores que se incorporan al proyecto.
- Integrantes del equipo de desarrollo.
- Revisores técnicos.
- Reclutadores o entrevistadores interesados en comprender la arquitectura.
- Estudiantes que deseen aprender sobre arquitecturas basadas en eventos y microservicios.

---

# Filosofía

La documentación fue escrita siguiendo una idea simple:

> **Cada documento responde una pregunta arquitectónica específica.**

En lugar de describir únicamente tecnologías, cada capítulo explica el problema que motivó una decisión de diseño, la solución adoptada y su implementación dentro de la plataforma.

Este enfoque busca facilitar la comprensión de la arquitectura y servir como referencia para la evolución futura del sistema.