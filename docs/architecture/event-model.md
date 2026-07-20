# Modelo de Eventos

## Propósito

Este documento describe el modelo de eventos utilizado por la plataforma para la comunicación entre microservicios.

Su objetivo es definir una estructura estándar para todos los eventos publicados dentro de la Arquitectura Orientada a Eventos (Event-Driven Architecture - EDA), garantizando consistencia, trazabilidad y evolución del sistema.

Este documento complementa:

- overview.md
- flow.md
- rabbitmq.md
- projections.md

---

# ¿Qué es un Evento?

Un evento representa un hecho del negocio que ya ocurrió.

No describe una intención ni una acción futura.

Representa un cambio de estado dentro del dominio.

Por ejemplo:

- Un producto fue creado.
- Un producto fue actualizado.
- Un producto fue activado.
- Un producto fue desactivado.
- El stock de un producto cambió.

Los eventos permiten que otros microservicios reaccionen a esos cambios sin depender directamente del servicio que los originó.

---

# Eventos como Hechos del Negocio

Una regla importante de esta arquitectura es que los eventos representan hechos consumados.

Incorrecto:

```
Actualizar Producto
```

Correcto:

```
Producto Actualizado
```

Incorrecto:

```
Crear Orden
```

Correcto:

```
Orden Creada
```

La diferencia parece pequeña, pero representa un cambio importante de paradigma.

Los consumidores no reciben órdenes.

Reciben información sobre algo que ya ocurrió.

---

# Objetivos del Modelo de Eventos

El modelo de eventos busca cumplir los siguientes objetivos:

- Estandarizar la comunicación entre microservicios.
- Facilitar la evolución de la plataforma.
- Garantizar trazabilidad.
- Reducir el acoplamiento.
- Mantener independencia entre productores y consumidores.

Todos los eventos publicados deben seguir la misma estructura.

---

# Estructura General

Todos los eventos heredan de una clase base denominada:

```
ProductEvent
```

Su estructura es la siguiente:

```
ProductEvent

├── eventId
├── eventType
├── eventVersion
├── aggregateId
├── traceId
└── occurredAt
```

Esta información representa los metadatos comunes a todos los eventos del dominio.

---

# Descripción de los Campos

## eventId

Identificador único del evento.

Cada evento publicado posee un identificador diferente.

Propósito:

- trazabilidad
- auditoría
- diagnóstico
- correlación de logs

Ejemplo:

```
8d2af33e-b22f-49c4-a8d0-b4ef67d91234
```

---

## eventType

Indica el tipo de evento publicado.

Ejemplos:

```
PRODUCT_CREATED

PRODUCT_UPDATED

PRODUCT_ACTIVATED

PRODUCT_DEACTIVATED

PRODUCT_STOCK_UPDATED
```

Este campo permite que los consumidores conozcan el significado del evento.

---

## eventVersion

Indica la versión del contrato del evento.

Ejemplo:

```
1.0
```

Este atributo permite evolucionar el modelo sin romper consumidores existentes.

En futuras versiones podrán coexistir distintos formatos del mismo evento.

---

## aggregateId

Identificador del Aggregate que originó el evento.

En Product Service corresponde al identificador del producto.

Ejemplo:

```
125
```

Los consumidores utilizan este valor para localizar la entidad correspondiente.

---

## traceId

Identificador único utilizado para seguir una operación distribuida entre múltiples microservicios.

Permite reconstruir el recorrido completo de una solicitud.

Ejemplo:

```
9f0bd4be55d84fdca671d43b0d63d321
```

Gracias al Trace ID es posible relacionar:

- logs
- eventos
- llamadas REST
- operaciones internas

Todo dentro de una misma ejecución.

---

## occurredAt

Fecha y hora exacta en que ocurrió el evento.

Ejemplo:

```
2026-07-20T18:42:13Z
```

No representa cuándo fue consumido.

Representa cuándo ocurrió el hecho del negocio.

---

# Eventos del Dominio de Productos

Actualmente Product Service publica los siguientes eventos.

## PRODUCT_CREATED

Se publica cuando un producto es creado.

Información principal:

- id
- nombre
- precio
- categoría
- stock inicial
- estado

---

## PRODUCT_UPDATED

Se publica cuando cambia información general del producto.

Ejemplos:

- nombre
- descripción
- categoría
- precio

No incluye modificaciones de stock.

---

## PRODUCT_ACTIVATED

Se publica cuando un producto vuelve a estar disponible.

---

## PRODUCT_DEACTIVATED

Se publica cuando un producto deja de estar disponible.

---

## PRODUCT_STOCK_UPDATED

Se publica exclusivamente cuando cambia el inventario disponible.

Separar este evento permite mantener una semántica clara y simplifica el procesamiento por parte de los consumidores.

---

# Eventos Específicos

Cada tipo de evento extiende la clase base agregando únicamente la información necesaria para representar ese hecho del negocio.

Ejemplo conceptual:

```
ProductEvent

↓

ProductCreatedEvent

↓

ProductUpdatedEvent

↓

ProductActivatedEvent

↓

ProductDeactivatedEvent

↓

ProductStockUpdatedEvent
```

De esta manera todos los eventos comparten los mismos metadatos, pero cada uno incorpora únicamente los datos propios de su operación.

---

# Principios del Modelo de Eventos

El diseño del modelo sigue los siguientes principios.

## Los eventos son inmutables

Una vez publicados no deben modificarse.

Representan un hecho histórico.

---

## Los eventos son independientes

Cada evento contiene toda la información necesaria para ser interpretado por un consumidor.

No requiere consultar al productor para comprender su significado.

---

## Los eventos describen el dominio

Los nombres deben expresar conceptos del negocio.

No operaciones técnicas.

---

## Versionado

Todo evento debe ser versionado.

Esto permite evolucionar el sistema manteniendo compatibilidad con consumidores existentes.

---

## Consistencia

Todos los eventos siguen exactamente la misma estructura base.

Esto simplifica el procesamiento y reduce la complejidad de los consumidores.

---

# Flujo de Vida de un Evento

El ciclo de vida de un evento dentro de la plataforma es el siguiente.

```
Operación de Negocio

↓

Persistencia

↓

Creación del Evento

↓

Publicación

↓

RabbitMQ

↓

Consumidor

↓

Procesamiento

↓

Actualización de la Proyección
```

El evento siempre se genera después de que la operación de negocio fue completada correctamente.

---

# Evolución del Modelo

Actualmente el modelo cubre únicamente el dominio de productos.

En el futuro otros dominios utilizarán el mismo estándar.

Por ejemplo:

```
OrderCreatedEvent

PaymentApprovedEvent

InventoryReservedEvent

ShipmentCreatedEvent

NotificationSentEvent
```

Todos compartirán la misma estructura base, permitiendo que la plataforma mantenga un modelo consistente de comunicación.

---

# Buenas Prácticas

Al diseñar nuevos eventos se recomienda seguir las siguientes reglas:

- Publicar únicamente hechos del negocio.
- Mantener nombres claros y consistentes.
- Evitar incluir información innecesaria.
- Versionar todos los contratos.
- Mantener los eventos inmutables.
- Reutilizar la estructura base.
- Agregar únicamente los datos específicos del evento.

---

# Conclusión

El modelo de eventos constituye el contrato de comunicación entre los microservicios de la plataforma.

Al definir una estructura común para todos los eventos se obtiene una comunicación consistente, desacoplada y preparada para evolucionar con el crecimiento del sistema.

Más que una representación técnica, los eventos constituyen el lenguaje mediante el cual los distintos dominios comparten información y colaboran sin depender directamente unos de otros.