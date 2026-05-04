# 📦 Product Service

## 🧠 Responsibility
Manages products, pricing, and stock within the e-commerce system.

---

## 🔄 Flow

- Retrieve available products
- Create and update products (admin)
- Validate stock
- Update inventory

### Product Status Lifecycle

- ACTIVE → visible and purchasable
- INACTIVE → hidden and not purchasable

---

## 📡 Endpoints

### GET /products
List all ACTIVE products

### GET /products/{id}
Get product details

### POST /products
Create a new product

### PUT /products/{id}
Update product information

### PATCH /products/{id}/activate
Activate product

### PATCH /products/{id}/deactivate
Deactivate product

### PATCH /products/{id}/stock
Update product stock

---

## 🔐 Security

- JWT-based authentication
- Integrated with user-service
- Roles:
    - ROLE_ADMIN → product management
    - ROLE_USER → read-only access

---

## 📡 Events

Publishes domain events:

- ProductCreated
- ProductUpdated
- ProductActivated
- ProductDeactivated

---

## 🔗 Integrations

- Consumed by:
    - order-service → stock and availability validation

---

## ⚠️ Error Handling

- Centralized error handling
- Business validations (e.g. non-negative stock)

---

## ⚙️ Tech

- Spring Boot
- PostgreSQL
- Spring Security (JWT)
- RabbitMQ (planned)