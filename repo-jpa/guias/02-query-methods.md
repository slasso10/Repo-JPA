# Guía 2: Query Methods en Spring Data JPA

> **Objetivo del examen:** Escribir 3 query methods. Siempre son variaciones de los mismos 3 patrones.

---

## Cómo funciona un query method

Spring genera el SQL automáticamente a partir del **nombre del método**. No tienes que escribir SQL.

```
findBy[Campo][Condición][Y/O][OtroCampo][Condición]
```

**El repositorio debe extender `JpaRepository`:**

```java
public interface BusRepository extends JpaRepository<Bus, Long> {
    // aquí van los query methods
}
```

---

## Los 3 patrones del examen

### Patrón 1 — Buscar por campo de entidad relacionada (navegar relación)

**Pregunta típica:** *"Encontrar los buses que atienden rutas de un tipo particular"*

Quiero buscar `Bus` filtrando por un campo (`tipo`) que está en `Ruta` (entidad relacionada).

```java
// Bus tiene → Ruta (campo llamado "ruta")
// Ruta tiene → String tipo

List<Bus> findByRutaTipo(String tipo);
//          ^^^^^^^^^^^^
//          [campo en Bus].[campo en Ruta]
//          ruta           tipo
```

**Otro ejemplo:** *"Hablantes que hablan lenguas clasificadas como Crítica"*

```java
// Hablante tiene → Lengua (campo llamado "lengua")
// Lengua tiene → String clasificacion

List<Hablante> findByLenguaClasificacion(String clasificacion);
```

**Regla:** `findBy` + nombre del campo de relación (con mayúscula) + nombre del campo en la entidad relacionada (con mayúscula)

---

### Patrón 2 — Obtener colección cruzando dos relaciones

**Pregunta típica:** *"Obtener todos los GeoPoints de buses que atienden una ruta específica"*

Quiero buscar `GeoPoint` filtrando por el `nombre` de la `Ruta`, que está dos niveles arriba.

```java
// GeoPoint tiene → Bus (campo "bus")
// Bus tiene → Ruta (campo "ruta")
// Ruta tiene → String nombre

List<GeoPoint> findByBusRutaNombre(String nombreRuta);
//              ^^^^^^^^^^^^^^^^
//              bus   ruta   nombre  (sin espacios, cada word capitalizada)
```

**Otro ejemplo:** *"Todos los registros sonoros de hablantes de una lengua específica"*

```java
// RegistroSonoro tiene → Hablante (campo "hablante")
// Hablante tiene → Lengua (campo "lengua")
// Lengua tiene → String nombre

List<RegistroSonoro> findByHablanteLenguaNombre(String nombreLengua);
```

**Regla:** Concatenar los nombres de los campos en la cadena de relaciones, cada uno con mayúscula inicial.

---

### Patrón 3 — El más reciente (Top1 + OrderBy + Desc)

**Pregunta típica:** *"Obtener la ubicación más reciente de un bus según su matrícula/placa"*

```java
// Quiero el GeoPoint más reciente (mayor timestamp) de un bus específico (por placa)
// GeoPoint tiene → Bus (campo "bus") → tiene String placa
// GeoPoint tiene → LocalDateTime timestamp

Optional<GeoPoint> findTop1ByBusPlacaOrderByTimestampDesc(String placa);
//                  ^^^^^ ^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                  Top1  filtro por placa  ordenar por timestamp descendente
```

**Otro ejemplo:** *"Registro sonoro más reciente de un hablante por código"*

```java
Optional<RegistroSonoro> findTop1ByHablanteCodigoOrderByTimestampDesc(String codigo);
```

**Desglose:**
- `findTop1` → solo el primer resultado
- `By[Campo][Campo]` → filtro
- `OrderBy[Campo]Desc` → ordenar de más nuevo a más viejo

---

## Tabla de palabras clave útiles

| Keyword | Ejemplo | SQL equivalente |
|---|---|---|
| `And` | `findByNombreAndTipo` | `WHERE nombre=? AND tipo=?` |
| `Or` | `findByNombreOrTipo` | `WHERE nombre=? OR tipo=?` |
| `OrderByXDesc` | `findByPlacaOrderByTimestampDesc` | `ORDER BY timestamp DESC` |
| `Top1` / `First` | `findTop1By...` | `LIMIT 1` |
| `Containing` | `findByNombreContaining` | `WHERE nombre LIKE '%?%'` |
| `IgnoreCase` | `findByNombreIgnoreCase` | `WHERE UPPER(nombre)=UPPER(?)` |
| `IsNull` | `findByFechaIsNull` | `WHERE fecha IS NULL` |
| `In` | `findByTipoIn(List<String> tipos)` | `WHERE tipo IN (...)` |

---

## @Query — cuando el nombre del método no alcanza

Si el query method queda muy largo o la lógica es compleja, usar `@Query` con JPQL:

```java
// JPQL usa nombres de CLASES y CAMPOS de Java, no nombres de tablas SQL
@Query("SELECT g FROM GeoPoint g WHERE g.bus.placa = :placa ORDER BY g.timestamp DESC")
List<GeoPoint> findRecorridoBus(@Param("placa") String placa);
```

```java
// Para el "más reciente":
@Query("SELECT g FROM GeoPoint g WHERE g.bus.placa = :placa ORDER BY g.timestamp DESC LIMIT 1")
Optional<GeoPoint> findUltimaUbicacion(@Param("placa") String placa);
```

> En JPQL: nombres de **clases** (GeoPoint, no geo_point) y nombres de **campos** (bus.placa, no bus_id).

---

## Estructura completa del repositorio

```java
package com.ejemplo.repository;

import com.ejemplo.model.GeoPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GeoPointRepository extends JpaRepository<GeoPoint, Long> {

    // Patrón 2: Todos los geopoints de una ruta específica
    List<GeoPoint> findByBusRutaNombre(String nombreRuta);

    // Patrón 3: El más reciente de un bus por placa
    Optional<GeoPoint> findTop1ByBusPlacaOrderByTimestampDesc(String placa);
}
```

```java
package com.ejemplo.repository;

import com.ejemplo.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {

    // Patrón 1: Buses por tipo de ruta
    List<Bus> findByRutaTipo(String tipo);
}
```

---

## Cómo leer el enunciado y mapear al query method

Cuando el parcial dice:

| Enunciado | Identifica | Query method |
|---|---|---|
| *"buses que atienden rutas de tipo X"* | Entidad: Bus, filtro: campo de Ruta | `findByRuta[Campo](valor)` |
| *"geopoints de buses de ruta X"* | Entidad: GeoPoint, 2 niveles arriba | `findByBusRuta[Campo](valor)` |
| *"más reciente de un [entidad] según [identificador]"* | Top1 + OrderBy + Desc | `findTop1By[Identificador]OrderBy[Timestamp]Desc(id)` |

**Identifica siempre:**
1. ¿Qué entidad devuelvo? → después de `find`
2. ¿Por qué filtro? → después de `By`, siguiendo la cadena de relaciones
3. ¿Necesito orden? → agrega `OrderBy[campo]Desc`
4. ¿Solo uno? → agrega `Top1`
