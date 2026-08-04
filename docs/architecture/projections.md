# Proyecciones

## Propósito

Este documento describe el concepto de proyección dentro de la arquitectura de la plataforma y explica por qué algunos microservicios mantienen una representación local de información perteneciente a otros dominios.

El objetivo es comprender cómo las proyecciones permiten reducir el acoplamiento entre servicios, mejorar el rendimiento y favorecer una Arquitectura Orientada a Eventos.

Este documento complementa:

- overview.md
- flow.md
- rabbitmq.md
- event-model.md

---

# El Problema

Order Service necesita conocer información de los productos para poder validar una compra.

Por ejemplo:

- nombre
- precio
- estado
- stock disponible

Sin embargo, toda esa información pertenece al dominio de Product Service.

La pregunta fue:

> ¿Debe Order Service consultar Product Service cada vez que un cliente crea una orden?

La respuesta fue **no**.

---

# La Solución

En lugar de realizar consultas REST continuamente, Order Service mantiene una representación local del catálogo de productos.

Esta representación recibe el nombre de **Proyección**.

```
Product Service

(Fuente de Verdad)

↓

Eventos

↓

ProductCatalog

(Proyección)
```

Cada vez que Product Service publica un evento, Order Service actualiza su proyección.

---

# ¿Qué es una Proyección?

Una proyección es una representación local de información perteneciente a otro dominio.

No es la entidad original.

No es la fuente de verdad.

No es una copia destinada a reemplazar al servicio propietario.

Su único objetivo consiste en permitir que un microservicio realice sus operaciones de forma independiente.

---

# Fuente de Verdad (Source of Truth)

Cada dominio posee un único responsable.

En el dominio de productos:

```
Product Service
```

es la Fuente de Verdad.

Esto significa que únicamente Product Service puede:

- crear productos
- modificar productos
- cambiar precios
- activar productos
- desactivar productos
- actualizar stock

Ningún otro microservicio puede modificar esta información.

---

# ProductCatalog

ProductCatalog representa la proyección local utilizada por Order Service.

Contiene únicamente la información necesaria para realizar operaciones de negocio.

Por ejemplo:

```
ProductCatalog

├── id
├── name
├── price
├── status
├── availableStock
├── categoryId
└── updatedAt
```

No intenta replicar completamente el modelo de Product Service.

Solo mantiene los datos que Order Service necesita.

---

# ¿Cómo se Mantiene Sincronizada?

Cada modificación realizada sobre un producto genera un evento.

Ejemplo:

```
Producto Actualizado

↓

ProductUpdatedEvent

↓

RabbitMQ

↓

Order Service

↓

Actualizar ProductCatalog
```

La sincronización ocurre automáticamente.

Order Service nunca consulta directamente la base de datos de Product Service.

---

# ¿Por Qué no Compartir la Base de Datos?

Una práctica común en sistemas pequeños consiste en que varios servicios compartan la misma base de datos.

Esta arquitectura evita completamente ese enfoque.

¿Por qué?

Porque compartir la base de datos genera:

- alto acoplamiento
- dependencia tecnológica
- dificultad para evolucionar
- problemas de escalabilidad

Cada microservicio debe ser propietario de su propia información.

---

# Beneficios de las Proyecciones

## Independencia

Cada servicio puede trabajar sin depender constantemente de otro.

---

## Menor Latencia

Las consultas se realizan sobre la base de datos local.

No existe una llamada REST por cada operación.

---

## Mayor Disponibilidad

Si Product Service se encuentra temporalmente fuera de línea, Order Service continúa disponiendo de la información previamente sincronizada.

---

## Escalabilidad

Cada servicio escala de manera independiente.

No existe un único punto de consulta para todo el sistema.

---

## Bajo Acoplamiento

Los servicios colaboran mediante eventos y no mediante consultas síncronas.

---

# Consistencia Eventual

Las proyecciones no se actualizan de manera instantánea.

Existe un pequeño intervalo entre:

```
Actualización del Producto

↓

Evento Publicado

↓

RabbitMQ

↓

Consumidor

↓

Actualización de ProductCatalog
```

Durante ese período ambos servicios pueden mostrar información diferente.

Este comportamiento es completamente esperado dentro de una Arquitectura Orientada a Eventos.

---

# ¿Qué Información Debe Contener una Proyección?

Una proyección debe almacenar únicamente la información necesaria para el dominio que la consume.

No debe replicar completamente otra entidad.

Ejemplo:

Order Service necesita:

- nombre
- precio
- stock
- estado

No necesita:

- proveedor
- costo
- imágenes
- historial
- auditoría

Mantener únicamente los datos necesarios reduce la complejidad y facilita la sincronización.

---

# Aprendizajes Durante el Desarrollo

Durante el diseño del proyecto surgieron varios aprendizajes importantes.

## ProductCatalog no es una Copia

Inicialmente parecía una duplicación de la tabla Product.

Con el tiempo se comprendió que representa una proyección del dominio de productos.

La diferencia es conceptual.

Una copia intenta reemplazar al original.

Una proyección únicamente facilita el trabajo del consumidor.

---

## Cada Dominio Mantiene su Responsabilidad

Product Service continúa siendo el único responsable del catálogo.

Order Service únicamente consume información.

Nunca modifica productos.

---

## Las Proyecciones Evolucionan de Forma Independiente

Si Order Service necesita nueva información para sus procesos, puede ampliar ProductCatalog sin afectar el modelo interno de Product Service.

Esto mantiene ambos dominios desacoplados.

---

# Evolución Futura

Actualmente existe una única proyección.

```
ProductCatalog
```

Sin embargo, la arquitectura permite incorporar muchas más.

Por ejemplo:

```
InventoryProjection

PaymentProjection

CustomerProjection

ShipmentProjection
```

Cada microservicio podrá mantener únicamente la información necesaria para su propio dominio.

---

# Conclusión

Las proyecciones constituyen uno de los mecanismos fundamentales para mantener el desacoplamiento entre microservicios dentro de una Arquitectura Orientada a Eventos.

En lugar de consultar continuamente otros servicios, cada dominio mantiene una representación local de la información que necesita para operar.

ProductCatalog es el primer ejemplo de este enfoque dentro de la plataforma.

Gracias a esta estrategia, Order Service puede validar órdenes utilizando información sincronizada mediante eventos, manteniendo a Product Service como la única Fuente de Verdad y evitando dependencias directas entre ambos dominios.