# 📦 Product Service

## 🧠 Responsibility
Gestión de productos, precios y stock dentro del sistema e-commerce.

---

## 🔄 Flow

- Consulta de productos disponibles
- Creación y actualización de productos (admin)
- Validación de stock
- Actualización de inventario

---

## 📡 Endpoints

### GET /products
Listar productos

### GET /products/{id}
Detalle producto

### POST /products
Crear producto

### PUT /products/{id}
Actualizar producto

### DELETE /products/{id}
Eliminar producto

### PATCH /products/{id}/stock
Actualizar stock

---

## 🔐 Security

- Protección mediante JWT
- Integración con el user-service
- Roles:
  - ROLE_ADMIN → gestión de productos
  - ROLE_USER → solo lectura

---

## 🔗 Integrations

- Consumido por futuros servicios:
  - order-service → validación de stock
- Preparado para eventos (ej: actualización de stock vía RabbitMQ)

---

## ⚠️ Error Handling

- Manejo centralizado de errores
- Validaciones de negocio (ej: stock no negativo)

---

## ⚙️ Tech

- Spring Boot
- PostgreSQL
- Spring Security (JWT)
- (Futuro) RabbitMQ