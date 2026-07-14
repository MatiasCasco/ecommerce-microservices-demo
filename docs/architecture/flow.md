# 🔄 Communication Flow

## 🧾 Flujo principal

1. El cliente se autentica en el **USER SERVICE**.
2. El cliente crea una orden en el **ORDER SERVICE**.
3. El **ORDER SERVICE**:
    - Extrae el userId desde el JWT.
    - Consulta el **ProductCatalog** local para validar:
        - existencia del producto
        - stock disponible
        - precio actual
4. Si la validación es correcta:
    - Guarda la orden.
    - Publica el evento **OrderCreatedEvent** en RabbitMQ.
5. El **NOTIFICATION SERVICE**:
    - Consume el evento.
    - Genera la notificación.
    - Guarda la información en MongoDB.
    - Reintenta mediante scheduler si ocurre un fallo.

---

## 🔄 Sincronización del catálogo

El catálogo de productos utilizado por ORDER SERVICE se mantiene sincronizado mediante eventos publicados por PRODUCT SERVICE.

Flujo:

Product Service
  ↓
ProductCreated / Updated / StockUpdated
  ↓
RabbitMQ
  ↓
ProductCatalogConsumer
  ↓
ProductCatalog

## 📌 Notas importantes

- ORDER SERVICE mantiene una proyección local sincronizada mediante eventos
- USER SERVICE no llama a otros servicios
- PRODUCT SERVICE es el Source of Truth del catálogo.
- NOTIFICATION SERVICE solo reacciona a eventos