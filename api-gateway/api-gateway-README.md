# 🌐 API Gateway

## 🧠 Responsibility

El API Gateway es el punto de entrada único al sistema.

Se encarga de:

- Routing hacia los microservicios
- Autenticación y validación de JWT
- Filtros (logging, seguridad, etc.)
- Centralizar el acceso del cliente

---

## 🏗️ Architecture Role

```plaintext
CLIENT → API GATEWAY → SERVICES