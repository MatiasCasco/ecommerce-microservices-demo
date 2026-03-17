# 📐 Design Patterns

| Pattern | Where | Purpose |
|--------|------|--------|
| Retry | REST / RabbitMQ | Reintentar fallos |
| Circuit Breaker | ORDER → PRODUCT | Evitar fallos en cascada |
| Idempotency | eventos | Evitar duplicados |
| Scheduler | Notification Service | Reprocesar eventos fallidos |
| Outbox Pattern | Order Service | Evitar pérdida de eventos |
| Consumer Pattern | Notification Service | Procesar eventos |
| DTO Pattern | APIs | Desacoplar entidades |

---

## 🧠 Explicación

### Retry
Permite reintentar llamadas fallidas (ej: caídas temporales)

### Circuit Breaker
Evita saturar servicios cuando están caídos

### Idempotency
Evita procesar eventos duplicados

### Outbox
Garantiza que los eventos se publiquen correctamente

### Scheduler
Reintenta procesos fallidos automáticamente