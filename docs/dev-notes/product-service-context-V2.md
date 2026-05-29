Proyecto: ecommerce-microservices-demo

Stack:
- Java / Spring Boot
- JWT (ROLE_USER / ROLE_ADMIN)
- Arquitectura de microservicios
- Logging estructurado + traceId
- JPA + Specification + Pageable

Estado actual:

✔ product-service:
- CRUD completo
- Soft delete reemplazado por ACTIVE / INACTIVE
- Endpoints:
    - activate / deactivate
- Filtros dinámicos con Specification
- Paginación y sorting funcionando
- Control por roles (ADMIN / USER)
- GET /products → solo ACTIVE para USER

✔ README actualizado

✔ Buenas prácticas aplicadas:
- Separación lógica negocio vs entidad
- Pageable + sort correcto
- Validaciones de dominio

Pendiente actual:

👉 Implementar event-driven (fase inicial SIN RabbitMQ):
- ProductEventPublisher (fake)
- Eventos:
    - ProductCreated
    - ProductUpdated
    - ProductActivated
    - ProductDeactivated

Objetivo:

👉 Implementar eventos correctamente antes de avanzar a RabbitMQ y order-service