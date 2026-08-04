# Product Service API

## Objetivo

Este documento describe la API pública expuesta por **Product Service**.

Su propósito es explicar el comportamiento funcional de cada endpoint, las reglas de negocio involucradas y las operaciones disponibles sobre el catálogo de productos.

La especificación técnica completa (OpenAPI/Swagger) representa la fuente oficial para contratos HTTP.

---

# Base Path

```
/products
```

---

# Recursos

Product Service expone operaciones sobre:

- Productos
- Categorías

---

# Endpoints de Productos

## Obtener Productos

### GET /products

Obtiene una lista paginada de productos.

### Características

- Specification API
- Paginación
- Ordenamiento
- Filtros dinámicos

### Casos de uso

- Catálogo público
- Panel administrativo
- Búsqueda de productos

---

## Obtener Producto

### GET /products/{id}

Obtiene el detalle de un producto.

### Validaciones

- El producto debe existir.

---

## Crear Producto

### POST /products

Crea un nuevo producto.

### Reglas de negocio

- El producto debe ser válido.
- El precio debe ser mayor a cero.
- El stock inicial no puede ser negativo.
- El producto se crea con estado ACTIVE.

### Evento publicado

```
PRODUCT_CREATED
```

---

## Actualizar Producto

### PUT /products/{id}

Actualiza la información general del producto.

### Información actualizada

- nombre
- descripción
- precio
- categoría

El stock no forma parte de esta operación.

### Evento publicado

```
PRODUCT_UPDATED
```

---

## Activar Producto

### PATCH /products/{id}/activate

Cambia el estado del producto a ACTIVE.

### Evento publicado

```
PRODUCT_ACTIVATED
```

---

## Desactivar Producto

### PATCH /products/{id}/deactivate

Cambia el estado del producto a INACTIVE.

### Evento publicado

```
PRODUCT_DEACTIVATED
```

---

## Actualizar Stock

### PATCH /products/{id}/stock

Actualiza únicamente la cantidad disponible.

### Justificación

El stock posee un caso de uso independiente debido a que representa una responsabilidad distinta dentro del dominio.

Separar esta operación evita modificar accidentalmente el inventario durante una actualización general del producto.

### Evento publicado

```
PRODUCT_STOCK_UPDATED
```

---

# Endpoints de Categorías

## Obtener Categorías

```
GET /categories
```

Obtiene todas las categorías registradas.

---

## Obtener Categoría

```
GET /categories/{id}
```

Obtiene el detalle de una categoría.

---

## Crear Categoría

```
POST /categories
```

Registra una nueva categoría.

---

## Actualizar Categoría

```
PUT /categories/{id}
```

Actualiza la información de una categoría.

---

## Eliminar Categoría

```
DELETE /categories/{id}
```

Elimina una categoría cuando las reglas del negocio lo permitan.

---

# Seguridad

La API utiliza autenticación basada en JWT.

Roles soportados:

| Rol | Permisos |
|------|-----------|
| ROLE_ADMIN | Administración completa del catálogo. |
| ROLE_USER | Consulta de productos. |

---

# Respuestas

Las respuestas utilizan un formato consistente para operaciones exitosas y errores.

Los errores de negocio son manejados mediante excepciones centralizadas y códigos de error compartidos en `common-lib`.

---

# Integración

Cada operación que modifica el catálogo genera un evento de dominio publicado mediante RabbitMQ.

Estos eventos permiten mantener sincronizados los consumidores sin necesidad de llamadas REST.

---

# Observaciones

La documentación funcional de la API se complementa con:

- domain.md
- product-flow.md
- event-publication.md
- synchronization.md

La documentación técnica de contratos HTTP se encuentra disponible mediante OpenAPI/Swagger.