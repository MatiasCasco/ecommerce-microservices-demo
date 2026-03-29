
# 🔔 Notification Service

## 🧠 Responsibility
Procesa eventos y genera notificaciones.

---

## 🔄 Flow

1. Consume eventos desde RabbitMQ
2. Genera notificación
3. Guarda en MongoDB
4. Reintenta fallos con scheduler

---

## 📡 Events

- OrderCreated

---

## ⚙️ Tech

- Spring Boot
- MongoDB
- RabbitMQ
- Scheduler

