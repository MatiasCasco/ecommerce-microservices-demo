# Logging

## Propósito

Este documento describe la estrategia de logging utilizada por la plataforma y explica cómo los registros permiten observar el comportamiento de los microservicios durante la ejecución.

El objetivo no consiste únicamente en almacenar mensajes, sino en generar información útil para diagnosticar problemas, comprender el flujo del negocio y facilitar la operación del sistema.

Este documento complementa:

- overview.md
- flow.md
- rabbitmq.md
- distributed-tracing.md

---

# El Problema

En una aplicación sencilla, imprimir mensajes en consola suele ser suficiente.

Por ejemplo:

```java
System.out.println("Producto creado");
```

Sin embargo, en una arquitectura de microservicios esto deja de ser útil rápidamente.

Cada servicio genera cientos o miles de registros.

Además:

- existen múltiples instancias
- múltiples consumidores
- múltiples eventos
- múltiples solicitudes simultáneas

La pregunta es:

> ¿Cómo encontramos la información correcta entre miles de registros?

---

# El Objetivo del Logging

El propósito del logging no es generar la mayor cantidad posible de mensajes.

Su objetivo es responder preguntas.

Por ejemplo:

- ¿Qué ocurrió?
- ¿Cuándo ocurrió?
- ¿Dónde ocurrió?
- ¿Qué operación estaba ejecutándose?
- ¿Qué datos fueron procesados?
- ¿Falló alguna operación?

Un buen sistema de logs permite responder estas preguntas sin necesidad de depurar el código.

---

# Logging dentro de la Plataforma

Todos los microservicios siguen la misma estrategia de logging.

```
Solicitud

↓

Lógica de Negocio

↓

CommerceLog

↓

Log4j2

↓

Archivo de Logs
```

Esto garantiza un formato consistente entre todos los servicios.

---

# Logging Estructurado

La plataforma utiliza logging estructurado.

En lugar de registrar únicamente texto:

```
Producto actualizado
```

Se registra información con contexto.

Ejemplo conceptual:

```
Nivel: INFO

Servicio: Product Service

Evento: PRODUCT_UPDATED

Trace ID: 9f0bd4...

Producto: 125

Mensaje: Producto actualizado correctamente
```

Este enfoque facilita la búsqueda y el análisis de los registros.

---

# Niveles de Logging

Cada mensaje posee un nivel de severidad.

## INFO

Describe operaciones normales del negocio.

Ejemplos:

- creación de productos
- actualización de órdenes
- publicación de eventos

---

## WARN

Representa situaciones inesperadas que no impiden continuar con la operación.

Ejemplos:

- configuraciones faltantes
- datos incompletos
- reintentos

---

## ERROR

Representa fallos que impiden completar una operación.

Ejemplos:

- excepciones
- errores de persistencia
- problemas de comunicación

---

## DEBUG

Contiene información útil durante el desarrollo.

Generalmente permanece deshabilitado en producción.

---

# Relación con la Trazabilidad

Cada registro incorpora automáticamente el Trace ID.

```
Solicitud

↓

TraceIdFilter

↓

MDC

↓

CommerceLog

↓

Log
```

Esto permite correlacionar registros pertenecientes a una misma operación distribuida.

---

# Logging de Eventos

La publicación y el consumo de eventos también generan registros.

Ejemplo:

```
Publicando PRODUCT_UPDATED

↓

RabbitMQ

↓

Consumidor recibe PRODUCT_UPDATED

↓

Actualización de ProductCatalog
```

Gracias al Trace ID es posible seguir todo el recorrido.

---

# Información Registrada

Cada log procura incluir únicamente la información necesaria para comprender la operación.

Generalmente contiene:

- nivel
- servicio
- evento
- Trace ID
- mensaje
- datos relevantes

Evitar registrar información innecesaria mejora la legibilidad de los registros.

---

# Buenas Prácticas

La plataforma sigue las siguientes recomendaciones.

## Registrar Eventos del Negocio

Los logs deben describir hechos relevantes del dominio.

No cada línea de código ejecutada.

---

## Evitar Información Sensible

Nunca deben registrarse:

- contraseñas
- tokens
- información financiera
- datos personales sensibles

---

## Mantener Consistencia

Todos los servicios utilizan el mismo formato.

Esto facilita la búsqueda y el análisis.

---

## Agregar Contexto

Un mensaje como:

```
Error
```

carece de utilidad.

Es preferible registrar:

- qué ocurrió
- dónde ocurrió
- qué entidad estaba involucrada
- Trace ID

---

# Implementación en la Plataforma

La estrategia de logging se implementa mediante componentes compartidos ubicados en **common-lib**.

```
common-lib

├── CommerceLog
├── LoggingConstants
└── Log4j2 Configuration
```

---

## CommerceLog

`CommerceLog` centraliza la generación de logs estructurados.

Su responsabilidad consiste en evitar que cada microservicio implemente su propia estrategia de logging.

Permite registrar información de forma consistente incluyendo automáticamente:

- Trace ID
- servicio
- evento
- mensaje
- datos adicionales

---

## LoggingConstants

Centraliza constantes relacionadas con eventos y categorías de logging.

Esto evita cadenas de texto repetidas en distintos servicios.

---

## Configuración de Log4j2

Log4j2 es el framework responsable de escribir los registros.

Su configuración define:

- formato
- destino
- nivel de logging
- rotación de archivos

Todos los microservicios utilizan una configuración homogénea.

---

# Aprendizajes Durante el Desarrollo

El desarrollo permitió comprender varios aspectos importantes.

## Los logs son parte de la arquitectura

Inicialmente se utilizaban únicamente para depuración.

Con el tiempo pasaron a convertirse en una herramienta fundamental para comprender el comportamiento distribuido del sistema.

---

## El contexto es más importante que el mensaje

Registrar:

```
Producto actualizado
```

es insuficiente.

Registrar además:

- Trace ID
- producto
- evento
- servicio

permite reconstruir completamente la operación.

---

## Logging y Trazabilidad trabajan juntos

Los logs adquieren verdadero valor cuando todos comparten el mismo Trace ID.

Sin trazabilidad, los registros permanecen aislados.

---

# Evolución Futura

La estrategia de logging podrá complementarse con herramientas de observabilidad como:

- ELK Stack (Elasticsearch, Logstash, Kibana)
- Grafana Loki
- OpenSearch
- CloudWatch Logs

Estas soluciones permitirán centralizar los registros de todos los microservicios y realizar búsquedas avanzadas sobre la plataforma.

---

# Conclusión

El logging constituye uno de los pilares de la observabilidad de la plataforma.

Más que registrar mensajes, proporciona el contexto necesario para comprender cómo se comportan los microservicios durante la ejecución.

Combinado con la trazabilidad distribuida y los eventos de negocio, el logging permite diagnosticar problemas, reconstruir operaciones y mantener una visión completa del comportamiento del sistema sin depender de herramientas de depuración.