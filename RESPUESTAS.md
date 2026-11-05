# Registro de productos de investigación – respuestas

## Archivos que agregué / modifiqué

| Archivo | Qué hace |
|---|---|
| `db/migration/V3__crear_productos.sql` | tabla `productos` (PK, FK semillero, FK autor, CHECKs de P3, índice único de P5) |
| `entity/Producto.java`, `repository/ProductoRepository.java` | mapeo JPA (`ddl-auto=validate` exige que coincida con V3) |
| `dto/request/ProductoRequest.java`, `RegistroProductosRequest.java` | Bean Validation de P2 y P3 |
| `dto/response/RegistroProductosResponse.java` | `semilleroId` + `totalGuardados` |
| `service/ProductoService.java` | `registrarProductos(...)` transaccional, aplica P1, P4 y P5 |
| `controller/ProductoController.java` | `POST /api/semilleros/{semilleroId}/productos` |
| `exception/RecursoNoEncontradoException.java` + `GlobalExceptionHandler` | 404 y 409 (el 400 ya existía con `ReglaNegocioException`) |
| `test/.../ProductoIntegrationTests.java` | pruebas de las reglas y del escenario del punto 1 |

## Anotación en el controlador

`@Valid` sobre el `@RequestBody`:

```java
public ResponseEntity<RegistroProductosResponse> registrar(
        @PathVariable Long semilleroId,
        @Valid @RequestBody RegistroProductosRequest request)
```

Sin `@Valid` Spring ignora las anotaciones del DTO. Además, en `RegistroProductosRequest`
la lista lleva `List<@Valid ProductoRequest>` para que también se validen los campos de
cada producto (si no, solo se validaría el tamaño de la lista).
Si falla, lanza `MethodArgumentNotValidException`, que el `GlobalExceptionHandler` ya traduce a 400.

## Mapa de reglas → dónde se cumplen

| Regla | Código HTTP | Dónde |
|---|---|---|
| P1 semillero existe | 404 | `ProductoService` (`RecursoNoEncontradoException`) |
| P2 1–10 productos | 400 | `@Size(min=1,max=10)` |
| P3 título/tipo/año | 400 | Bean Validation + `CHECK` en la BD |
| P4 autor del semillero | 400 | `ProductoService` (`ReglaNegocioException`) |
| P5 título no repetido | 400 | `ProductoService` + índice único `uq_producto_semillero_titulo` |
| P6 todo o nada | – | `@Transactional` + validar antes de `saveAll` |

Nota sobre P5: el enunciado pide 400 para título repetido, así que el servicio responde 400.
El 409 lo dejé para el caso raro de concurrencia: si dos peticiones pasan la validación al mismo
tiempo, el índice único de la BD rechaza la segunda y el handler de `DataIntegrityViolationException`
la convierte en 409.

## Escenario: 3 productos, el tercero con `codigoAutor` que no pertenece al semillero

**1. ¿Qué código HTTP se espera?**  
**400 Bad Request** (`ReglaNegocioException` por P4). El semillero sí existe, por eso no es 404.

**Consulta SQL (antes y después, misma consulta):**

```sql
SELECT COUNT(*) FROM productos WHERE semillero_id = :semilleroId;
```

| Momento | Resultado |
|---|---|
| Antes de la petición | N (por ejemplo 0) |
| Después de la petición | N (el mismo valor, no aumentó en 1, 2 ni 3) |

**2. ¿Por qué mi implementación garantiza esto? (2 líneas)**  
El servicio recorre y valida todos los productos (P4 y P5) y solo después llama a `saveAll`, así que al
fallar el tercero todavía no se ha hecho ningún INSERT. Además el método es `@Transactional`: cualquier
`RuntimeException` (como `ReglaNegocioException`) hace rollback de lo que se hubiera alcanzado a escribir.

## Cosas que no hice perfectas (siendo honesto)

- No pude compilar ni correr los tests aquí (no hay Maven ni acceso a Maven Central en mi entorno), así que
  hay que correr `./mvnw test` con PostgreSQL arriba para confirmar.
- La BD solo garantiza que `autor_id` existe en `integrantes`, no que sea del mismo semillero (P4 se
  valida solo en el servicio). Para reforzarlo se podría hacer una FK compuesta `(semillero_id, autor_id)`
  con un `UNIQUE (semillero_id, id)` en `integrantes`.
- El tipo `articulo` va sin tilde porque así lo pide el enunciado.
