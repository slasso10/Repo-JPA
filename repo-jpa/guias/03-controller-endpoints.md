# Guía 3: Controller con Endpoints GET

> **Objetivo del examen:** Crear 3 endpoints GET que prueben los 3 query methods.
> El examen permite romper el patrón (sin service), ir directo de controller a repository.

---

## Estructura mínima del controller

```java
package com.ejemplo.controller;

import com.ejemplo.model.*;
import com.ejemplo.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")   // ← prefijo base de todas las rutas
public class MiController {

    // Inyección directa del repositorio (el examen permite saltarse el service)
    private final BusRepository busRepository;
    private final GeoPointRepository geoPointRepository;

    // Constructor (forma recomendada de inyección)
    public MiController(BusRepository busRepository, GeoPointRepository geoPointRepository) {
        this.busRepository = busRepository;
        this.geoPointRepository = geoPointRepository;
    }

    // ─── Endpoint 1 ───────────────────────────────────────────────────────────
    // Patrón 1: Buscar por campo de entidad relacionada
    @GetMapping("/buses/tipo/{tipo}")
    public List<Bus> getBusesPorTipo(@PathVariable String tipo) {
        return busRepository.findByRutaTipo(tipo);
    }

    // ─── Endpoint 2 ───────────────────────────────────────────────────────────
    // Patrón 2: Obtener colección cruzando relaciones
    @GetMapping("/geopoints/ruta/{nombre}")
    public List<GeoPoint> getGeopointsPorRuta(@PathVariable String nombre) {
        return geoPointRepository.findByBusRutaNombre(nombre);
    }

    // ─── Endpoint 3 ───────────────────────────────────────────────────────────
    // Patrón 3: Obtener el más reciente
    @GetMapping("/geopoints/reciente/{placa}")
    public Optional<GeoPoint> getUltimaUbicacion(@PathVariable String placa) {
        return geoPointRepository.findTop1ByBusPlacaOrderByTimestampDesc(placa);
    }
}
```

---

## Las anotaciones que necesitas

| Anotación | Uso | Ejemplo URL |
|---|---|---|
| `@RestController` | Marca la clase como controller REST (responde JSON) | — |
| `@RequestMapping("/api")` | Prefijo de todas las rutas de este controller | — |
| `@GetMapping("/ruta")` | Endpoint HTTP GET | GET /api/ruta |
| `@PathVariable` | Captura segmento de la URL | `/buses/{placa}` → `@PathVariable String placa` |
| `@RequestParam` | Captura parámetro de query string | `/buses?tipo=X` → `@RequestParam String tipo` |

---

## @PathVariable vs @RequestParam

```java
// Con @PathVariable — la variable va EN la URL
@GetMapping("/buses/{placa}")
public Bus getBus(@PathVariable String placa) { ... }
// URL: GET /api/buses/ABC123

// Con @RequestParam — la variable va como parámetro
@GetMapping("/buses")
public List<Bus> getBuses(@RequestParam String tipo) { ... }
// URL: GET /api/buses?tipo=Alimentador
```

> Para el examen ambos funcionan. `@PathVariable` es más limpio para IDs y nombres únicos.

---

## Controller completo para el modelo MetroCali

```java
package com.metrocali.controller;

import com.metrocali.model.*;
import com.metrocali.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MetroCaliController {

    private final BusRepository busRepository;
    private final GeoPointRepository geoPointRepository;

    public MetroCaliController(BusRepository busRepository,
                                GeoPointRepository geoPointRepository) {
        this.busRepository = busRepository;
        this.geoPointRepository = geoPointRepository;
    }

    // 1. Buses que atienden rutas de un tipo (ej: Alimentador)
    @GetMapping("/buses/tipo/{tipo}")
    public List<Bus> getBusesPorTipoRuta(@PathVariable String tipo) {
        return busRepository.findByRutaTipo(tipo);
    }

    // 2. Todos los geopoints de buses que atienden una ruta específica (ej: T31)
    @GetMapping("/geopoints/ruta/{nombre}")
    public List<GeoPoint> getGeopointsPorRuta(@PathVariable String nombre) {
        return geoPointRepository.findByBusRutaNombre(nombre);
    }

    // 3. Ubicación más reciente de un bus por placa
    @GetMapping("/geopoints/reciente/{placa}")
    public Optional<GeoPoint> getUltimaUbicacion(@PathVariable String placa) {
        return geoPointRepository.findTop1ByBusPlacaOrderByTimestampDesc(placa);
    }
}
```

---

## Cómo probar los endpoints (sin Postman)

Con H2 cargado, abrir en el navegador:

```
GET http://localhost:8080/api/buses/tipo/Alimentador
GET http://localhost:8080/api/geopoints/ruta/T31
GET http://localhost:8080/api/geopoints/reciente/ABC123
```

O desde terminal:
```bash
curl http://localhost:8080/api/buses/tipo/Alimentador
```

---

## Errores frecuentes en el controller

| Error | Causa | Solución |
|---|---|---|
| 404 Not Found | URL mal escrita o `@RequestMapping` incorrecto | Verificar la URL exacta |
| 500 Internal Server Error | NullPointerException o query method incorrecto | Ver logs en consola |
| Respuesta `[]` vacía | Query method correcto pero datos no coinciden | Verificar datos en H2 console |
| `Could not write JSON` (error 500) | Ciclo infinito en serialización JSON por relación bidireccional | Agregar `@JsonIgnore` en el lado inverso |

### Solución al ciclo infinito JSON

Si `Ruta` tiene `List<Bus>` y `Bus` tiene `Ruta`, Jackson entra en bucle:

```java
// En la entidad Ruta (o la que tiene @OneToMany)
@JsonIgnore  // ← añadir esto
@OneToMany(mappedBy = "ruta")
private List<Bus> buses = new ArrayList<>();
```

O usar `@JsonManagedReference` / `@JsonBackReference`:

```java
// En Ruta (lado padre)
@JsonManagedReference
@OneToMany(mappedBy = "ruta")
private List<Bus> buses;

// En Bus (lado hijo)
@JsonBackReference
@ManyToOne
@JoinColumn(name = "ruta_id")
private Ruta ruta;
```
