# ecommerce-microservices-demo

## Stack actual

- Java 21
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL
- Spring Data JPA
- Specification API
- Pageable + Sorting
- Log4j2
- Arquitectura de microservicios
- Event-Driven Architecture (fase inicial)
- Logging estructurado + traceId
- common-lib compartida

---

# Estado actual del proyecto

## product-service

Actualmente implementado:

### Seguridad

- JWT Authentication
- ROLE_ADMIN / ROLE_USER
- CustomAuthenticationEntryPoint
- CustomAccessDeniedHandler
- TraceIdFilter integrado en SecurityFilterChain

---

### Logging y observabilidad

Implementado:

- CommerceLog
- traceId con MDC
- logs estructurados
- ProductEventPublisher con logging estructurado
- eventId y traceId diferenciados
- configuración Log4j2 custom
- rolling logs

Formato actual:

```text
module=PRODUCT_SERVICE
event=PRODUCT_CREATED
traceId=...
eventId=...
productId=...
occurredAt=...