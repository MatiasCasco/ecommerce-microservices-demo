# ADR-001: Use RabbitMQ for async communication

## Status
Accepted

## Context
Se necesita desacoplar los servicios y manejar eventos de forma confiable.

## Decision
Se utilizará RabbitMQ como broker de mensajería.

## Consequences

### Positivas
- Desacoplamiento entre servicios
- Mejor escalabilidad
- Mayor resiliencia

### Negativas
- Mayor complejidad
- Consistencia eventual