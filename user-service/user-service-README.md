# 👤 User Service

## 🧠 Responsibility
Gestión de usuarios y autenticación.

---

## 🔄 Flow

1. Registro de usuario
2. Login
3. Generación de JWT

---

## 📡 Endpoints

### POST /auth/register
Registrar usuario

### POST /auth/login
Login


---

## 🔐 Security

- Autenticación basada en JWT
- Uso de OAuth2 (client credentials) para comunicación entre servicios

---

## 🔗 Integrations

- Emite tokens JWT para otros servicios
- Puede ser consumido por otros microservicios para validación de usuarios

---

## ⚠️ Error Handling

- Manejo centralizado de errores
- Uso de CustomResponseErrorHandler
- Soporte para reintentos (RetryableException)

---

## ⚙️ Tech

- Spring Boot
- PostgreSQL
- JWT