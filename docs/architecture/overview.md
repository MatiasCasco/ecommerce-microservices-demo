# 🏗️ Architecture Overview

## 📊 System Diagram

![Architecture](./diagrams/architecture.png)

![ArchitectureV3](./diagrams/architectureV3.png)

---

## 🧠 Description

Este sistema está basado en una arquitectura de microservicios orientada a eventos.

### Servicios principales:

- **USER SERVICE**
  - Autenticación y gestión de usuarios
  - Base de datos: PostgreSQL

- **PRODUCT SERVICE**
  - Gestión de productos, precios y stock
  - Fuente oficial de información (Source of Truth)
  - Publica eventos de dominio mediante RabbitMQ
  - Base de datos: PostgreSQL

- **ORDER SERVICE**
  - Gestión y orquestación de órdenes
  - Mantiene una proyección local del catálogo de productos
  - Consume eventos publicados por Product Service
  - Valida productos utilizando ProductCatalog
  - Base de datos: PostgreSQL

- **NOTIFICATION SERVICE**
  - Consume eventos y genera notificaciones
  - Base de datos: MongoDB
  - Incluye scheduler para reintentos

- **RABBITMQ**
  - Broker de mensajería
  - Distribuye eventos de dominio entre microservicios
  - Desacopla productores y consumidores

---

## ⚙️ Arquitectura

- Comunicación síncrona: REST
- Comunicación asíncrona: RabbitMQ
- Arquitectura orientada a eventos (EDA)
- Product Service actúa como Source of Truth
- Order Service mantiene una proyección local sincronizada mediante eventos
- Servicios desacoplados