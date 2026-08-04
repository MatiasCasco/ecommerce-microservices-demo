# Trazabilidad Distribuida

## Propósito

Este documento describe cómo la plataforma realiza el seguimiento de una misma operación a través de múltiples microservicios mediante el uso de un identificador de trazabilidad (Trace ID).

El objetivo es comprender por qué la trazabilidad distribuida es un componente fundamental en una arquitectura de microservicios y cómo permite diagnosticar problemas, correlacionar eventos y facilitar la observabilidad del sistema.

Este documento complementa:

- overview.md
- flow.md
- rabbitmq.md
- event-model.md
- projections.md

---

# El Problema

En una aplicación monolítica, una solicitud generalmente se procesa dentro del mismo proceso.

Cuando ocurre un error resulta sencillo identificar:

- qué operación falló
- qué método fue ejecutado
- qué información fue procesada

Sin embargo, en una arquitectura de microservicios una única operación puede atravesar múltiples servicios.

Por ejemplo:

```
Cliente

↓

API Gateway

↓

Order Service

↓

RabbitMQ

↓

Product Service

↓

Notification Service
```

Cada servicio genera sus propios logs.

La pregunta es:

> ¿Cómo sabemos que todos esos logs pertenecen a la misma solicitud?

---

# La Solución

La plataforma asigna un identificador único denominado **Trace ID** al inicio de cada operación.

Ese identificador acompaña toda la ejecución.

```
Trace ID

↓

Gateway

↓

Order Service

↓

RabbitMQ

↓

Product Service

↓

Notification Service
```

Todos los servicios registran exactamente el mismo identificador.

Gracias a esto es posible reconstruir el recorrido completo de una solicitud.

---

# ¿Qué es un Trace ID?

Un Trace ID es un identificador único que representa una operación distribuida.

No identifica:

- un usuario
- un producto
- una orden

Identifica una única ejecución del sistema.

Ejemplo:

```
9f0bd4be55d84fdca671d43b0d63d321
```

Todas las operaciones relacionadas con esa solicitud compartirán el mismo Trace ID.

---

# Objetivos

La trazabilidad distribuida permite:

- correlacionar logs
- seguir solicitudes
- reconstruir el recorrido de una operación
- facilitar el diagnóstico de errores
- mejorar la observabilidad del sistema

---

# Flujo del Trace ID

El recorrido de un Trace ID dentro de la plataforma es el siguiente.

```
Cliente

↓

API Gateway

↓

TraceIdFilter

↓

HTTP Header

↓

Order Service

↓

RabbitMQ

↓

Evento

↓

Product Service

↓

Logs
```

El mismo identificador permanece durante todo el recorrido.

---

# Generación del Trace ID

Cuando una solicitud ingresa al sistema:

- si el cliente ya envía un Trace ID, se reutiliza.
- si no existe, la plataforma genera uno automáticamente.

De esta manera todas las solicitudes quedan correctamente identificadas.

---

# Propagación entre Servicios

Cada vez que un servicio realiza una llamada hacia otro servicio, el Trace ID debe propagarse.

Ejemplo:

```
Gateway

↓

Trace ID

↓

Order Service

↓

HTTP Header

↓

Product Service
```

Todos los servicios continúan trabajando con el mismo identificador.

---

# Propagación mediante Eventos

La trazabilidad no se limita a llamadas HTTP.

Los eventos también transportan el Trace ID.

```
Order Service

↓

ProductUpdatedEvent

↓

RabbitMQ

↓

Product Service
```

Dentro del evento:

```
ProductEvent

├── eventId

├── traceId

└── occurredAt
```

Esto permite relacionar los logs del productor con los del consumidor.

---

# Logging Correlacionado

Todos los logs incluyen el mismo Trace ID.

Ejemplo conceptual:

```
[TRACE_ID=9f0bd4...]

Gateway

↓

Order Service

↓

RabbitMQ

↓

Product Service
```

Al buscar ese identificador es posible visualizar toda la operación.

---

# MDC (Mapped Diagnostic Context)

La plataforma utiliza MDC para almacenar temporalmente el Trace ID durante el procesamiento de una solicitud.

Gracias a esto no es necesario enviar el identificador manualmente a cada log.

Cada registro generado durante la ejecución incorpora automáticamente el mismo Trace ID.

---

# Beneficios

La trazabilidad distribuida aporta múltiples ventajas.

## Diagnóstico

Permite localizar rápidamente dónde ocurrió un problema.

---

## Correlación

Relaciona logs pertenecientes a una misma operación.

---

## Observabilidad

Facilita comprender el comportamiento completo del sistema.

---

## Auditoría

Permite reconstruir el recorrido de una solicitud incluso tiempo después de haberse ejecutado.

---

## Mantenimiento

Reduce considerablemente el tiempo necesario para investigar incidentes.

---

# Buenas Prácticas

Toda operación distribuida debería:

- generar un único Trace ID
- propagar el Trace ID entre servicios
- incluir el Trace ID en los eventos
- registrar el Trace ID en todos los logs
- conservar el mismo Trace ID durante toda la ejecución

---

# Aprendizajes Durante el Desarrollo

La implementación permitió comprender varias cuestiones importantes.

## Los logs aislados no son suficientes

Cada microservicio posee su propio archivo de logs.

Sin un identificador común resulta muy difícil reconstruir una operación completa.

---

## Los eventos también forman parte de la trazabilidad

Inicialmente los eventos no incluían el Trace ID.

Durante el desarrollo se observó que esto impedía relacionar el productor con el consumidor.

Por esta razón todos los eventos incorporan el atributo `traceId`.

---

## La trazabilidad comienza en el ingreso de la solicitud

El Trace ID debe generarse o recuperarse únicamente al inicio del procesamiento.

Todos los servicios posteriores reutilizan el mismo valor.

---

# Evolución Futura

La implementación actual constituye una base para futuras mejoras de observabilidad.

Entre ellas:

- OpenTelemetry
- Jaeger
- Zipkin
- AWS X-Ray
- Grafana Tempo

Estas herramientas permitirán visualizar gráficamente el recorrido completo de una solicitud entre todos los microservicios.

---

# Conclusión

La trazabilidad distribuida constituye uno de los pilares de la observabilidad en una arquitectura de microservicios.

Al asignar un único Trace ID a cada operación y propagarlo entre servicios, eventos y registros de logs, es posible reconstruir el recorrido completo de una solicitud, simplificar el diagnóstico de errores y comprender el comportamiento del sistema de manera integral.

En esta plataforma, el Trace ID actúa como el vínculo común que conecta todas las operaciones distribuidas, permitiendo que los distintos componentes colaboren manteniendo una visión unificada de cada proceso de negocio.

---

# Implementación en la Plataforma

La trazabilidad distribuida de la plataforma se implementa mediante un conjunto de componentes compartidos ubicados en **common-lib**.

Cada componente posee una responsabilidad específica dentro del ciclo de vida del Trace ID.

```
common-lib

├── TraceConstants
├── TraceIdFilter
├── TraceIdInterceptor
└── CommerceLog
```

En conjunto, estos componentes permiten que todas las solicitudes HTTP, eventos y registros de logs compartan el mismo identificador de trazabilidad.

---

## TraceConstants

`TraceConstants` centraliza todas las constantes relacionadas con la trazabilidad.

Su responsabilidad consiste en evitar valores duplicados o literales distribuidos por el código.

Entre ellas:

- nombre del header HTTP
- nombre utilizado dentro del MDC
- constantes compartidas por toda la plataforma

Al centralizar estos valores se facilita el mantenimiento y se garantiza consistencia entre los distintos microservicios.

---

## TraceIdFilter

`TraceIdFilter` representa el punto de entrada de la trazabilidad.

Su responsabilidad consiste en interceptar todas las solicitudes HTTP entrantes.

Durante este proceso:

1. Verifica si la solicitud ya contiene un Trace ID.
2. Si existe, lo reutiliza.
3. Si no existe, genera uno nuevo.
4. Lo almacena dentro del MDC.
5. Lo incorpora a la respuesta cuando corresponde.

Su funcionamiento puede resumirse de la siguiente manera.

```
Solicitud HTTP

↓

TraceIdFilter

↓

Obtener o Generar Trace ID

↓

MDC

↓

Continuar procesamiento
```

De esta manera toda la ejecución utilizará exactamente el mismo identificador.

---

## TraceIdInterceptor

Cuando un microservicio realiza una llamada HTTP hacia otro servicio, el Trace ID debe continuar propagándose.

`TraceIdInterceptor` es responsable de esta tarea.

Su función consiste en:

- obtener el Trace ID desde el MDC
- agregarlo automáticamente al header HTTP saliente
- garantizar que el siguiente microservicio continúe utilizando el mismo identificador

Flujo simplificado:

```
Order Service

↓

TraceIdInterceptor

↓

HTTP Header

↓

Product Service
```

Gracias a este mecanismo la trazabilidad atraviesa múltiples servicios sin intervención del código de negocio.

---

## CommerceLog

La plataforma utiliza `CommerceLog` como componente centralizado para la generación de logs estructurados.

Además de estandarizar el formato de los registros, incorpora automáticamente el Trace ID almacenado en el MDC.

Esto permite que todos los logs generados durante una misma operación compartan el mismo identificador.

Ejemplo conceptual:

```
INFO

Trace ID: 9f0bd4...

Evento: PRODUCT_UPDATED

Servicio: Product Service
```

De esta manera resulta posible reconstruir toda la ejecución buscando únicamente el Trace ID.

---

## Trace ID dentro de los Eventos

La trazabilidad no finaliza en las solicitudes HTTP.

Cuando Product Service publica un evento, el Trace ID también forma parte del mensaje.

Ejemplo conceptual:

```
ProductEvent

├── eventId

├── eventType

├── aggregateId

├── traceId

└── occurredAt
```

Cuando Order Service consume ese evento, el mismo Trace ID queda disponible para continuar registrando logs relacionados con la misma operación.

Esto permite correlacionar:

- solicitudes HTTP
- publicación de eventos
- procesamiento de consumidores
- registros de logs

Todo utilizando un único identificador.

---

## Flujo Completo de la Implementación

El recorrido completo del Trace ID dentro de la plataforma puede resumirse de la siguiente manera.

```
Cliente

↓

API Gateway

↓

TraceIdFilter

↓

MDC

↓

Lógica de Negocio

↓

CommerceLog

↓

ProductEvent (traceId)

↓

RabbitMQ

↓

Order Service

↓

Consumer

↓

CommerceLog
```

Durante todo este recorrido el Trace ID permanece inalterado.

Esto permite seguir una operación completa desde que ingresa al sistema hasta que finaliza su procesamiento en los distintos microservicios.

---