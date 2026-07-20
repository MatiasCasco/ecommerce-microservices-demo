# Biblioteca Compartida

## Propósito

Este documento describe el propósito y las responsabilidades del módulo **common-lib**, utilizado por los distintos microservicios de la plataforma.

El objetivo de este módulo es centralizar capacidades técnicas reutilizables, evitando duplicación de código y manteniendo consistencia entre los servicios, sin generar acoplamiento entre los dominios de negocio.

Este documento complementa:

- overview.md
- distributed-tracing.md
- logging.md
- security.md

---

# El Problema

En una arquitectura de microservicios es habitual que varios servicios necesiten funcionalidades similares.

Por ejemplo:

- manejo de errores
- logging
- trazabilidad
- utilidades JWT
- respuestas comunes
- constantes compartidas

Implementar estas funcionalidades en cada microservicio provoca:

- duplicación de código
- inconsistencias
- mayor esfuerzo de mantenimiento

La plataforma necesitaba una solución para compartir estas capacidades sin compartir lógica de negocio.

---

# La Solución

Se creó un módulo independiente denominado **common-lib**.

Este módulo contiene únicamente componentes técnicos reutilizables.

```
                common-lib

      ┌──────────┼──────────┐
      │          │          │
      ▼          ▼          ▼

 Product     Order     Notification
 Service     Service      Service
```

Todos los microservicios pueden utilizar estos componentes manteniendo la independencia de sus dominios.

---

# ¿Qué es common-lib?

`common-lib` es un módulo compartido que agrupa funcionalidades transversales de la plataforma.

No contiene lógica de negocio.

No conoce entidades específicas.

No depende de ningún microservicio.

Su única responsabilidad consiste en proporcionar capacidades reutilizables.

---

# Principios de Diseño

La biblioteca compartida sigue los siguientes principios.

## Independencia del Dominio

Nunca contiene:

- Product
- Order
- Customer
- Payment

Estos conceptos pertenecen a los dominios correspondientes.

---

## Reutilización

Todo componente incluido debe poder ser utilizado por múltiples servicios.

---

## Bajo Acoplamiento

Los microservicios dependen de common-lib.

common-lib no depende de ellos.

Esto evita dependencias circulares.

---

# Componentes Compartidos

Actualmente la biblioteca incluye diferentes categorías de componentes.

---

## Seguridad

Responsables de la autenticación mediante JWT.

Ejemplos:

- JwtUtil

---

## Trazabilidad

Componentes relacionados con el seguimiento distribuido.

Ejemplos:

- TraceConstants
- TraceIdFilter
- TraceIdInterceptor

---

## Logging

Componentes utilizados para registrar información estructurada.

Ejemplos:

- CommerceLog
- LoggingConstants

---

## Manejo de Errores

Componentes para estandarizar las respuestas de error.

Ejemplos:

- BaseException
- ErrorResponse
- ErrorCode
- GlobalExceptionHandler

---

## Utilidades

Clases auxiliares compartidas entre múltiples servicios.

Siempre que no representen lógica de negocio.

---

# ¿Qué NO debe estar en common-lib?

Una regla importante consiste en evitar incorporar componentes específicos del dominio.

Ejemplos incorrectos:

```
ProductService

OrderRepository

CustomerEntity

PaymentValidator
```

Estos componentes pertenecen exclusivamente a sus respectivos microservicios.

---

# Dependencias

La relación entre los módulos puede resumirse de la siguiente manera.

```
             common-lib
                  ▲
                  │
     ┌────────────┼────────────┐
     │            │            │
     ▼            ▼            ▼

 Product     Order      Notification
 Service     Service        Service
```

Todos los microservicios dependen de common-lib.

common-lib no depende de ninguno de ellos.

---

# Beneficios

## Consistencia

Todos los servicios utilizan exactamente las mismas implementaciones.

---

## Menor Duplicación

Evita copiar código entre microservicios.

---

## Mantenimiento

Una mejora realizada en common-lib puede beneficiar a todos los servicios que la utilizan.

---

## Evolución

Las capacidades técnicas evolucionan de forma independiente al negocio.

---

# Implementación en la Plataforma

Actualmente el módulo contiene componentes como:

```
common-lib

├── security
│   └── JwtUtil
│
├── tracing
│   ├── TraceConstants
│   ├── TraceIdFilter
│   └── TraceIdInterceptor
│
├── logging
│   ├── CommerceLog
│   └── LoggingConstants
│
├── exception
│   ├── BaseException
│   ├── ErrorCode
│   ├── ErrorResponse
│   └── GlobalExceptionHandler
│
└── util
```

La estructura puede evolucionar incorporando nuevas capacidades técnicas reutilizables.

---

# Buenas Prácticas

Antes de agregar un nuevo componente a common-lib conviene responder las siguientes preguntas.

- ¿Puede ser utilizado por varios microservicios?
- ¿Es independiente del dominio?
- ¿Representa una capacidad técnica?
- ¿Evita duplicación de código?

Si la respuesta es negativa, probablemente no deba formar parte de la biblioteca compartida.

---

# Aprendizajes Durante el Desarrollo

El desarrollo permitió comprender varios aspectos importantes.

## Compartir código no significa compartir dominio

Inicialmente parecía conveniente reutilizar modelos de negocio.

Con el tiempo se observó que esto incrementaba el acoplamiento entre microservicios.

---

## Las capacidades técnicas sí pueden compartirse

Logging, trazabilidad, seguridad y manejo de errores son responsabilidades transversales.

Centralizarlas mejora la consistencia sin afectar la independencia de los dominios.

---

## Menos es más

No todo debe incorporarse a common-lib.

La biblioteca debe mantenerse pequeña y enfocada únicamente en componentes realmente compartidos.

---

# Evolución Futura

En el futuro podrían incorporarse nuevas capacidades como:

- clientes HTTP reutilizables
- validadores comunes
- métricas
- resiliencia
- utilidades para eventos
- soporte para OpenTelemetry

Siempre respetando el principio de independencia del dominio.

---

# Conclusión

La biblioteca compartida constituye un mecanismo para reutilizar capacidades técnicas sin comprometer la independencia de los microservicios.

Al mantener separadas las responsabilidades de infraestructura y las reglas de negocio, la plataforma consigue reducir la duplicación de código, mejorar la consistencia entre servicios y facilitar la evolución de la arquitectura sin generar acoplamiento entre dominios.