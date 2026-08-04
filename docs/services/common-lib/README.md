# Common Library

## Descripción

Common Library es la librería compartida utilizada por todos los microservicios del ecosistema **Ecommerce Microservices Demo**.

Su objetivo es centralizar la infraestructura común del sistema, proporcionando componentes reutilizables que permiten mantener consistencia, reducir duplicación de código y facilitar la evolución de la arquitectura.

Common Library no implementa lógica de negocio.

Su responsabilidad consiste en ofrecer contratos compartidos y componentes transversales para todos los servicios.

---

# Responsabilidades

Actualmente la librería proporciona:

- Eventos compartidos del dominio.
- Logging estructurado.
- Manejo centralizado de errores.
- Excepciones compartidas.
- Componentes de seguridad.
- Utilidades para trazabilidad distribuida.

---

# Arquitectura

Todos los microservicios dependen de Common Library.

```text
                     Common Library

          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
      Logging        Security        Trace
          │              │              │
          ▼              ▼              ▼
      Exceptions      Errors         Events
          │
          ▼
────────────────────────────────────────────────

       User Service

       Product Service

       Order Service

      Future Services
```

Common Library representa la infraestructura compartida del ecosistema.

---

# Componentes

## Event

Contiene el contrato compartido utilizado por la arquitectura Event-Driven.

Incluye:

- ProductEvent
- ProductCreatedEvent
- ProductUpdatedEvent
- ProductActivatedEvent
- ProductDeactivatedEvent
- ProductStockUpdatedEvent

Además de:

- EventType
- EventRoutingKey
- EventConstants

Estos contratos permiten la comunicación entre Product Service y los consumidores mediante RabbitMQ.

---

## Logging

Proporciona un mecanismo unificado de logging estructurado.

Componente principal:

- CommerceLog

Permite registrar información consistente en todos los microservicios incluyendo:

- módulo
- evento
- mensaje
- path
- traceId
- datos adicionales

---

## Error Handling

Define un modelo común para el manejo de errores.

Incluye:

- ErrorCode
- GlobalErrorCode
- ErrorResponse

Todos los servicios comparten el mismo formato de respuesta ante errores.

---

## Exceptions

Centraliza las excepciones reutilizables.

Incluye:

- BaseException

Permite mantener una jerarquía consistente de excepciones entre todos los microservicios.

---

## Security

Contiene componentes reutilizables relacionados con autenticación.

Actualmente incluye:

- JwtUtil

Evita duplicar la lógica relacionada con JWT en cada servicio.

---

## Trace

Proporciona componentes para la trazabilidad distribuida.

Incluye:

- TraceConstants

Estos componentes permiten mantener un identificador único (TraceId) durante toda la ejecución de una operación distribuida.

---

# Principios de Diseño

Common Library fue diseñada siguiendo los siguientes principios.

## Reutilización

Toda funcionalidad utilizada por más de un microservicio debe evaluarse para incorporarse en Common Library.

---

## Consistencia

Todos los servicios utilizan los mismos contratos compartidos.

Esto garantiza uniformidad en:

- eventos
- errores
- logging
- seguridad

---

## Bajo Acoplamiento

La librería no conoce la lógica de negocio de ningún microservicio.

Únicamente proporciona infraestructura reutilizable.

---

## Evolución Controlada

Los cambios en Common Library deben preservar la compatibilidad con los consumidores existentes.

Toda modificación de contratos compartidos debe evaluarse cuidadosamente.

---

# Dependencias

Actualmente Common Library es utilizada por:

- User Service
- Product Service
- Order Service

En futuras versiones también será utilizada por:

- Inventory Service
- Payment Service
- Notification Service
- Analytics Service

---

# Tecnologías

- Java 21
- Spring Boot
- Jackson
- JWT
- Log4j2

---

# Estado

✅ Librería compartida utilizada por todos los microservicios del proyecto.

Actualmente constituye la base de infraestructura del ecosistema, centralizando contratos, componentes reutilizables y funcionalidades transversales necesarias para mantener consistencia entre los servicios.