# RabbitMQ y Comunicación Basada en Eventos

## Propósito

Este documento describe cómo se comunican los microservicios de la plataforma mediante RabbitMQ utilizando una Arquitectura Orientada a Eventos (Event-Driven Architecture - EDA).

Más que explicar qué es RabbitMQ, el objetivo es comprender las decisiones arquitectónicas que llevaron a su incorporación, cómo fluye la información entre los servicios y cuáles son los beneficios que aporta este modelo de comunicación.

Este documento complementa la documentación de arquitectura:

- overview.md
- flow.md
- event-model.md
- projections.md

---

# El Problema

Uno de los primeros desafíos arquitectónicos del proyecto fue definir cómo debían comunicarse los microservicios.

El Product Service es el responsable de administrar el catálogo de productos, mientras que el Order Service necesita conocer dicha información para validar las compras.

La pregunta fue:

> ¿Cómo puede Order Service disponer siempre de la información más reciente de los productos sin depender directamente de Product Service?

La solución más evidente parecía ser una comunicación mediante REST.

```
Order Service
      │
      │ HTTP
      ▼
Product Service
```

Sin embargo, esta arquitectura presenta varios inconvenientes cuando el sistema comienza a crecer.

---

# ¿Por qué REST no era suficiente?

REST es una excelente alternativa cuando una operación requiere una respuesta inmediata.

Sin embargo, utilizar REST como mecanismo principal de comunicación entre microservicios genera varios problemas arquitectónicos.

## Alto Acoplamiento

Order Service depende directamente de Product Service.

Si Product Service deja de estar disponible, Order Service puede verse imposibilitado de continuar con su operación.

```
Order Service

↓

Product Service
```

Los servicios dejan de ser independientes.

---

## Mayor Latencia

Cada operación requiere una comunicación adicional por red.

```
Cliente

↓

Order Service

↓

REST

↓

Product Service

↓

Base de Datos
```

Cada llamada incrementa el tiempo total de respuesta.

---

## Fallos en Cascada

Si Product Service presenta:

- tiempos de espera elevados
- mantenimiento
- despliegues
- indisponibilidad

Order Service también se verá afectado.

Un problema en un servicio puede propagarse al resto del sistema.

---

## Menor Escalabilidad

Cada consulta al catálogo implica una nueva petición hacia Product Service.

Con el crecimiento del sistema, este servicio puede convertirse en un cuello de botella.

---

# Arquitectura Orientada a Eventos

En lugar de consultar constantemente la información del catálogo, Product Service informa automáticamente al resto del sistema cuando ocurre un cambio relevante.

En lugar de preguntar:

> "¿Cuál es el estado actual del producto?"

Los demás servicios reciben eventos como:

- Producto Creado
- Producto Actualizado
- Producto Activado
- Producto Desactivado
- Stock Actualizado

El modelo de comunicación cambia completamente.

```
            Product Service

                    │

             Publica Evento

                    │

                RabbitMQ

                    │

        ┌───────────┴───────────┐

        ▼                       ▼

Order Service          Futuros Servicios
```

El productor desconoce quién consumirá el evento.

Cada consumidor decide qué información necesita.

Esto reduce significativamente el acoplamiento entre servicios.

---

# ¿Por qué RabbitMQ?

RabbitMQ fue seleccionado como la infraestructura de mensajería porque permite establecer una comunicación asíncrona, confiable y desacoplada entre microservicios.

Sus responsabilidades son:

- recibir eventos
- enrutar mensajes
- almacenar temporalmente los eventos
- entregar mensajes a los consumidores
- facilitar la escalabilidad futura
- permitir la incorporación de mecanismos de recuperación

RabbitMQ no ejecuta lógica de negocio.

Su única responsabilidad es transportar eventos.

---

# Publicación de Eventos

Cada vez que Product Service completa correctamente una operación de negocio, publica un evento.

Ejemplo:

```
Actualizar Producto

↓

Guardar en Base de Datos

↓

Crear ProductUpdatedEvent

↓

Publicar Evento

↓

RabbitMQ
```

Primero finaliza la operación de negocio.

Luego se comunica lo ocurrido.

Los eventos representan hechos que ya sucedieron.

---

# Consumo de Eventos

Cada microservicio decide qué eventos necesita consumir.

Actualmente:

```
Product Service

↓

RabbitMQ

↓

Order Service

↓

Proyección ProductCatalog
```

En el futuro podrán incorporarse nuevos consumidores sin modificar Product Service.

Por ejemplo:

- Inventory Service
- Notification Service
- Search Service
- Recommendation Service
- Analytics Service

Esta es una de las principales ventajas de una Arquitectura Orientada a Eventos.

---

# Componentes de RabbitMQ

## Productor (Producer)

El Productor es responsable de generar y publicar eventos.

Actualmente:

```
Product Service
```

Publica los siguientes eventos:

- PRODUCT_CREATED
- PRODUCT_UPDATED
- PRODUCT_ACTIVATED
- PRODUCT_DEACTIVATED
- PRODUCT_STOCK_UPDATED

---

## Exchange

El Exchange recibe todos los eventos publicados.

Su responsabilidad consiste en decidir hacia qué colas deben enviarse.

Actualmente:

```
product.exchange
```

El productor nunca envía mensajes directamente a las colas.

Siempre publica hacia un Exchange.

```
Productor

↓

Exchange

↓

Colas
```

---

## Routing Keys

Las Routing Keys clasifican cada evento.

Actualmente:

```
product.created

product.updated

product.activated

product.deactivated

product.stock.updated
```

Estas claves permiten que RabbitMQ determine qué colas deben recibir cada mensaje.

---

## Colas (Queues)

Las colas almacenan temporalmente los mensajes hasta que un consumidor pueda procesarlos.

Actualmente existen:

```
product.created.queue

product.updated.queue

product.activated.queue

product.deactivated.queue

product.stock.updated.queue
```

En el futuro podrán existir nuevas colas para nuevos microservicios.

---

## Consumidor (Consumer)

Los consumidores reciben y procesan los eventos.

Actualmente:

```
Order Service
```

Su responsabilidad consiste en mantener sincronizada la proyección ProductCatalog.

---

# Modelo de Eventos

Todos los eventos comparten una estructura común.

```
ProductEvent

├── eventId

├── eventType

├── eventVersion

├── aggregateId

├── traceId

└── occurredAt
```

Esta estructura permite que todos los servicios procesen eventos de forma consistente.

---

# Eventos Actuales

## PRODUCT_CREATED

Representa la creación de un nuevo producto.

Su objetivo es sincronizar ProductCatalog.

---

## PRODUCT_UPDATED

Representa modificaciones generales sobre un producto.

Ejemplos:

- nombre
- descripción
- categoría
- precio

Este evento no incluye modificaciones de stock.

---

## PRODUCT_ACTIVATED

Representa la activación de un producto.

Los consumidores actualizan el estado correspondiente.

---

## PRODUCT_DEACTIVATED

Representa la desactivación de un producto.

Los consumidores dejan de permitir futuras operaciones sobre dicho producto.

---

## PRODUCT_STOCK_UPDATED

Representa modificaciones sobre el inventario.

Este evento existe exclusivamente para sincronizar el stock.

Separar el stock del resto de las actualizaciones simplifica la lógica de los consumidores y evita inconsistencias.

---

# Flujo Completo de un Evento

Ejemplo: actualización del precio de un producto.

```
Cliente

↓

PUT /products/{id}

↓

Product Service

↓

Base de Datos

↓

ProductUpdatedEvent

↓

RabbitMQ Exchange

↓

Queue

↓

Order Service

↓

ProductCatalog

↓

Sincronización Finalizada
```

El cliente nunca interactúa directamente con RabbitMQ.

Toda la comunicación ocurre de manera interna dentro de la plataforma.

---

# ProductCatalog como Proyección

Order Service no es propietario de la información de los productos.

Product Service continúa siendo la Fuente de Verdad (Source of Truth).

Order Service mantiene únicamente una proyección local.

```
Product Service

(Fuente de Verdad)

↓

Eventos

↓

ProductCatalog

(Proyección Local)
```

Su objetivo es optimizar las operaciones de negocio sin depender constantemente de Product Service.

---

# Consistencia Eventual

Debido a que la comunicación es asíncrona, la sincronización no ocurre de manera instantánea.

```
Producto Actualizado

↓

Evento Publicado

↓

Mensaje Entregado

↓

Proyección Actualizada
```

Durante ese breve intervalo ambos servicios pueden observar estados diferentes.

Este comportamiento se conoce como Consistencia Eventual.

La arquitectura acepta este compromiso a cambio de obtener:

- independencia
- escalabilidad
- resiliencia

---

# Beneficios Arquitectónicos

La incorporación de RabbitMQ aporta múltiples beneficios.

## Bajo Acoplamiento

Los servicios dejan de depender unos de otros de forma síncrona.

---

## Escalabilidad

Es posible incorporar nuevos consumidores sin modificar los productores.

---

## Resiliencia

Los mensajes permanecen almacenados hasta que puedan ser procesados correctamente.

---

## Extensibilidad

Nuevos microservicios pueden comenzar a consumir eventos existentes sin afectar la arquitectura actual.

---

## Separación de Responsabilidades

Cada servicio mantiene la responsabilidad exclusiva sobre su dominio.

La comunicación se realiza mediante eventos de negocio.

---

# Compromisos de Diseño (Trade-offs)

Toda decisión arquitectónica implica ventajas y desventajas.

La Arquitectura Orientada a Eventos no es la excepción.

## Mayor Complejidad

Es necesario administrar una infraestructura adicional de mensajería.

---

## Consistencia Eventual

Los datos no se sincronizan de forma inmediata.

---

## Observabilidad

Los sistemas distribuidos requieren:

- logging estructurado
- Trace IDs
- monitoreo
- seguimiento de eventos

---

## Manejo de Errores

Los consumidores deben estar preparados para manejar fallos durante el procesamiento.

En futuras versiones se incorporarán mecanismos de recuperación.

---

# Aprendizajes Durante el Desarrollo

La implementación permitió descubrir varias decisiones importantes.

## Separar los Eventos de Negocio

Inicialmente el stock formaba parte de una actualización general.

Durante el desarrollo se comprendió que el stock posee reglas de negocio propias.

Como consecuencia se separaron:

- PRODUCT_UPDATED
- PRODUCT_STOCK_UPDATED

Esta decisión redujo inconsistencias y simplificó la sincronización.

---

## ProductCatalog no es una Copia

Inicialmente parecía una simple duplicación de datos.

Con el tiempo quedó claro que ProductCatalog representa una proyección del dominio de productos.

Su objetivo es permitir que Order Service trabaje de forma independiente manteniendo a Product Service como Fuente de Verdad.

---

## Los Eventos Representan Hechos

Los eventos no representan acciones futuras.

Representan hechos que ya ocurrieron.

No publicamos:

```
Actualizar Producto
```

Publicamos:

```
Producto Actualizado
```

Esta diferencia simplifica la comunicación distribuida.

---

## La Comunicación Debe Ser Orientada al Dominio

Los eventos describen hechos del negocio.

No describen operaciones técnicas.

Los consumidores comprenden qué ocurrió, sin conocer cómo ocurrió.

---

# Evolución Futura

La implementación actual representa la primera etapa de la arquitectura de mensajería.

Las siguientes mejoras forman parte del roadmap del proyecto.

## Retry

Reintentar automáticamente el procesamiento de eventos fallidos.

---

## Dead Letter Queue (DLQ)

Almacenar mensajes que no pudieron procesarse correctamente.

---

## Publisher Confirms

Garantizar que RabbitMQ recibió correctamente cada evento publicado.

---

## Consumidores Idempotentes

Evitar el procesamiento duplicado de mensajes.

---

## Outbox Pattern

Garantizar la publicación confiable de eventos junto con la persistencia en la base de datos.

---

## Trazabilidad Distribuida

Seguir un mismo evento a través de múltiples microservicios utilizando Trace IDs.

---

# Conclusión

RabbitMQ no representa únicamente un broker de mensajería dentro de la plataforma.

Constituye la base de la Arquitectura Orientada a Eventos que permite que los microservicios colaboren de manera independiente, desacoplada y escalable.

En lugar de intercambiar solicitudes síncronas, los servicios comunican hechos del negocio mediante eventos, permitiendo que nuevos consumidores puedan incorporarse sin modificar los servicios existentes.

La implementación actual establece una base sólida sobre la cual podrán incorporarse patrones avanzados como Retry, Dead Letter Queue, Publisher Confirms, Idempotencia y Outbox Pattern, fortaleciendo progresivamente la confiabilidad y escalabilidad del sistema.