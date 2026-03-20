# 🔄 Communication Flow

## 🧾 Flujo principal

1. El cliente se autentica en el **USER SERVICE**
2. El cliente realiza una orden en el **ORDER SERVICE**
3. El ORDER SERVICE:
   - Extrae el userId desde el JWT
   - Consulta el **PRODUCT SERVICE** para validar stock y precio
4. Si todo es válido:
   - Guarda la orden
   - Publica un evento `OrderCreated` en RabbitMQ
5. El **NOTIFICATION SERVICE**:
   - Consume el evento
   - Genera notificación
   - Guarda en MongoDB
   - Reintenta en caso de fallo mediante scheduler

---

## 📌 Notas importantes

- ORDER SERVICE es el orquestador
- USER SERVICE no llama a otros servicios
- PRODUCT SERVICE es independiente
- NOTIFICATION SERVICE solo reacciona a eventos