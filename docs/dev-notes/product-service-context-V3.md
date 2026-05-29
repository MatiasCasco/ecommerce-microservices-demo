# Revisión de ProductEventPublisher

## Estado actual

```java
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProductEventPublisher.class);

    private static final String COMPONENT = "PRODUCT_SERVICE";
    private static final String PUBLISHING = "Publishing event";

    public void publish(ProductEvent event) {

        event.setTraceId(MDC.get(TraceConstants.TRACE_ID));

        LOGGER.info(CommerceLog.info(
                COMPONENT,
                event.getEventType(),
                PUBLISHING,
                null,
                Map.of(
                        "traceId", MDC.get(TraceConstants.TRACE_ID),
                        "eventId", event.getEventId(),
                        "productId", event.getAggregateId(),
                        "occurredAt", event.getOccurredAt()
                )
        ).toString());

        rabbitTemplate.convertAndSend(
                EventConstants.PRODUCT_EXCHANGE,
                event.getRoutingKey(),
                event
        );
    }
}
```

---

# Lo que está bien

## Publisher genérico

```java
public void publish(ProductEvent event)
```

Ventajas:

- No depende de eventos específicos.
- Permite publicar cualquier evento que extienda `ProductEvent`.
- Aprovecha polimorfismo.

---

## Propagación de TraceId

```java
event.setTraceId(MDC.get(TraceConstants.TRACE_ID));
```

Permite transportar el contexto distribuido desde:

```text
HTTP Request
    ↓
Product Service
    ↓
RabbitMQ Event
    ↓
Consumer
```

---

## Logging estructurado

```java
LOGGER.info(...)
```

Ventajas:

- Facilita debugging.
- Permite correlacionar logs mediante `traceId`.
- Mantiene observabilidad distribuida.

---

## Routing dinámico

```java
event.getRoutingKey()
```

Ventajas:

- El publisher no conoce detalles de cada evento.
- Cada evento define su propia routing key.
- Diseño desacoplado.

---

# Mejoras recomendadas

## Evitar múltiples llamadas a MDC

### Actual

```java
event.setTraceId(MDC.get(TraceConstants.TRACE_ID));

Map.of(
    "traceId", MDC.get(TraceConstants.TRACE_ID)
)
```

### Recomendado

```java
String traceId = MDC.get(TraceConstants.TRACE_ID);

event.setTraceId(traceId);
```

---

## Registrar publicación exitosa

Actualmente:

```text
Publishing event
```

se registra antes de enviar el mensaje.

Si RabbitMQ falla:

```text
Publishing event
ERROR
```

puede generar confusión.

Más adelante:

```java
LOGGER.info("Publishing event");

rabbitTemplate.convertAndSend(...);

LOGGER.info("Event published");
```

---

## Manejo de excepciones

Actualmente:

```java
rabbitTemplate.convertAndSend(...)
```

puede lanzar:

```java
AmqpException
```

Más adelante evaluar:

```java
try {
    rabbitTemplate.convertAndSend(...);
} catch (AmqpException ex) {
    ...
}
```

---

# Riesgo arquitectónico futuro

## Escenario actual

```text
Guardar en BD
      ↓
Publicar evento
```

Ejemplo:

```java
repository.save(product);

publisher.publish(event);
```

---

## Problema

¿Qué ocurre si?

```text
DB OK
RabbitMQ ERROR
```

Resultado:

```text
Producto persistido
Evento NO publicado
```

Se genera inconsistencia entre:

- Base de datos
- Sistema de eventos

---

## Solución futura

```text
Outbox Pattern
```

No implementar todavía.

Primero:

- Producer
- Exchange
- Routing Keys
- Queue
- Consumer
- RabbitMQ básico

---

# Versión recomendada

```java
public void publish(ProductEvent event) {

    String traceId = MDC.get(TraceConstants.TRACE_ID);

    event.setTraceId(traceId);

    LOGGER.info(
            CommerceLog.info(
                    COMPONENT,
                    event.getEventType(),
                    PUBLISHING,
                    null,
                    Map.of(
                            "traceId", traceId,
                            "eventId", event.getEventId(),
                            "productId", event.getAggregateId(),
                            "occurredAt", event.getOccurredAt()
                    )
            ).toString()
    );

    rabbitTemplate.convertAndSend(
            EventConstants.PRODUCT_EXCHANGE,
            event.getRoutingKey(),
            event
    );
}
```

---

# Evaluación actual

| Aspecto | Estado |
|----------|----------|
| Diseño OO | ⭐⭐⭐⭐⭐ |
| EDA | ⭐⭐⭐⭐⭐ |
| RabbitMQ | ⭐⭐⭐☆☆ |
| Observabilidad | ⭐⭐⭐⭐⭐ |
| Escalabilidad | ⭐⭐⭐⭐☆ |
| Mantenibilidad | ⭐⭐⭐⭐⭐ |

---

# Próximos pasos

## Prioridad Alta

- [ ] Verificar publicación real en RabbitMQ.
- [ ] Verificar JSON generado.
- [ ] Verificar traceId dentro del evento.
- [ ] Crear primer Consumer (`@RabbitListener`).

## Prioridad Media

- [ ] DLQ.
- [ ] Retry.
- [ ] Publisher Confirms.

## Prioridad Avanzada

- [ ] Outbox Pattern.
- [ ] Idempotencia.
- [ ] CorrelationId.
- [ ] Observabilidad distribuida completa.