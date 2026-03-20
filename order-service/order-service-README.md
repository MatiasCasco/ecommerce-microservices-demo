# 📦 Order Service

## 🧠 Responsibility
Gestiona la creación y procesamiento de órdenes.

---

## 🔗 Dependencies

- Product Service (REST)
- RabbitMQ (event publishing)

---

## 🔄 Flow

1. Recibe request del cliente
2. Valida usuario desde JWT
3. Consulta PRODUCT SERVICE:
   - stock
   - precio
4. Guarda la orden en PostgreSQL
5. Publica evento `OrderCreated`

---

## 📡 Endpoints

### POST /orders
Crear una orden

### GET /orders/{id}
Obtener una orden

---

## ⚙️ Tech

- Spring Boot
- PostgreSQL
- RabbitMQ