# 📦 Product Catalog

## 📖 Overview

`ProductCatalog` es una proyección local del catálogo de productos mantenida por **Order Service**.

Su propósito es disponer de la información necesaria para validar órdenes sin realizar consultas REST al **Product Service**.

El **Product Service** continúa siendo el propietario de la información (Source of Truth), mientras que **Order Service** mantiene una copia sincronizada mediante eventos.

---

## 🎯 Objetivo

El catálogo local permite:

- Validar la existencia de un producto.
- Consultar el precio vigente.
- Verificar el stock disponible.
- Conocer el estado del producto (ACTIVE / INACTIVE).

De esta forma, la creación de órdenes no depende de llamadas síncronas a otro microservicio.

---

## 🏗️ Arquitectura

```text
                Product Service
            (Source of Truth)

                   Product
                      │
          ProductCreatedEvent
          ProductUpdatedEvent
          ProductActivatedEvent
          ProductDeactivatedEvent
          ProductStockUpdatedEvent
                      │
                  RabbitMQ
                      │
             ProductCatalogConsumer
                      │
             ProductCatalogService
                      │
                 ProductCatalog
```

---

## 🔄 Sincronización

El catálogo se mantiene sincronizado mediante eventos publicados por **Product Service**.

Cada vez que un producto cambia, se publica un evento que es consumido por **Order Service**.

| Evento | Acción |
|----------|--------|
| ProductCreatedEvent | Crear registro |
| ProductUpdatedEvent | Actualizar información |
| ProductActivatedEvent | Cambiar estado a ACTIVE |
| ProductDeactivatedEvent | Cambiar estado a INACTIVE |
| ProductStockUpdatedEvent | Actualizar stock |

---

## 📋 Información almacenada

Actualmente `ProductCatalog` almacena únicamente la información necesaria para crear órdenes.

Ejemplo:

- id
- name
- price
- availableStock
- status
- categoryId
- updatedAt

No replica toda la información del Product Service.

---

## ⚙️ Flujo de actualización

```text
Product actualizado

↓

Product Service

↓

ProductUpdatedEvent

↓

RabbitMQ

↓

ProductCatalogConsumer

↓

ProductCatalogService

↓

ProductCatalog
```

---

## ✅ Beneficios

- Reduce el acoplamiento entre microservicios.
- Evita llamadas REST durante la creación de órdenes.
- Mejora el rendimiento al disponer de información local.
- Permite que Order Service continúe operando aunque Product Service no esté disponible temporalmente.
- Facilita la escalabilidad independiente de ambos servicios.

---

## ⚠️ Consideraciones

Al utilizar eventos, la sincronización es de **consistencia eventual**.

Esto implica que puede existir un pequeño intervalo entre la actualización realizada en **Product Service** y su propagación al **Order Service**.

Sin embargo, este enfoque reduce significativamente el acoplamiento entre servicios y mejora la resiliencia de la arquitectura.