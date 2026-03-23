# Solución Completa — Examen Lenguas en Peligro

**Contexto:** Archivo de Lenguas en Peligro gestiona Lenguas, Hablantes y Registros Sonoros.
- Una Lengua → muchos Hablantes
- Un Hablante → muchos Registros Sonoros (con timestamp)
- Cada Lengua tiene una clasificación (Amenazada, Crítica, Vigente)

---

## Paso 1: Entidades corregidas [20%]

### Lengua.java
```java
package com.lenguas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lengua")
public class Lengua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String clasificacion;  // Amenazada, Critica, Vigente

    @JsonIgnore
    @OneToMany(mappedBy = "lengua")
    private List<Hablante> hablantes = new ArrayList<>();
}
```

### Hablante.java
```java
package com.lenguas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "hablante")
public class Hablante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String codigo;  // identificador único del hablante

    @ManyToOne
    @JoinColumn(name = "lengua_id")
    private Lengua lengua;

    @JsonIgnore
    @OneToMany(mappedBy = "hablante")
    private List<RegistroSonoro> registros = new ArrayList<>();
}
```

### RegistroSonoro.java
```java
package com.lenguas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "registro_sonoro")
public class RegistroSonoro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private String tipo;  // frase, canto, narracion
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "hablante_id")
    private Hablante hablante;
}
```

---

## Paso 2: data.sql completo

```sql
-- Lenguas
INSERT INTO lengua (id, nombre, clasificacion) VALUES (1, 'Nasa Yuwe', 'Amenazada');
INSERT INTO lengua (id, nombre, clasificacion) VALUES (2, 'Wayuunaiki', 'Vigente');
INSERT INTO lengua (id, nombre, clasificacion) VALUES (3, 'Cubeo', 'Critica');
INSERT INTO lengua (id, nombre, clasificacion) VALUES (4, 'Totoró', 'Critica');

-- Hablantes (lengua_id referencia a lengua.id)
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (1, 'Ana Torres', 'HAB001', 1);
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (2, 'Carlos Paz', 'HAB002', 1);
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (3, 'María Luna', 'HAB003', 2);
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (4, 'Pedro Ruiz', 'HAB004', 3);
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (5, 'Lucia Vega', 'HAB005', 4);

-- Registros Sonoros (hablante_id referencia a hablante.id)
INSERT INTO registro_sonoro (id, descripcion, tipo, timestamp, hablante_id)
VALUES (1, 'Canción de bienvenida', 'canto', '2024-03-01 10:00:00', 1);
INSERT INTO registro_sonoro (id, descripcion, tipo, timestamp, hablante_id)
VALUES (2, 'Frase cotidiana', 'frase', '2024-03-15 14:30:00', 1);
INSERT INTO registro_sonoro (id, descripcion, tipo, timestamp, hablante_id)
VALUES (3, 'Historia del origen', 'narracion', '2024-03-20 09:00:00', 1);
INSERT INTO registro_sonoro (id, descripcion, tipo, timestamp, hablante_id)
VALUES (4, 'Saludo tradicional', 'frase', '2024-02-10 11:00:00', 2);
INSERT INTO registro_sonoro (id, descripcion, tipo, timestamp, hablante_id)
VALUES (5, 'Canto ritual', 'canto', '2024-01-05 08:00:00', 4);
INSERT INTO registro_sonoro (id, descripcion, tipo, timestamp, hablante_id)
VALUES (6, 'Narración tradicional', 'narracion', '2024-04-01 15:00:00', 4);
```

---

## Paso 3: Repositorios con Query Methods [60%]

### LenguaRepository.java
```java
package com.lenguas.repository;

import com.lenguas.model.Lengua;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LenguaRepository extends JpaRepository<Lengua, Long> {
    // No se necesitan query methods aquí para el examen
}
```

### HablanteRepository.java
```java
package com.lenguas.repository;

import com.lenguas.model.Hablante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HablanteRepository extends JpaRepository<Hablante, Long> {

    // [20%] Hablantes que pertenecen a lenguas de una clasificación particular
    // Ejemplo: hablantes de lenguas clasificadas como "Critica"
    List<Hablante> findByLenguaClasificacion(String clasificacion);
}
```

### RegistroSonoroRepository.java
```java
package com.lenguas.repository;

import com.lenguas.model.RegistroSonoro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistroSonoroRepository extends JpaRepository<RegistroSonoro, Long> {

    // [20%] Todos los registros sonoros producidos por hablantes de una lengua específica
    // Ejemplo: registros de la lengua "Nasa Yuwe"
    List<RegistroSonoro> findByHablanteLenguaNombre(String nombreLengua);

    // [20%] Registro más reciente de un hablante según su código
    Optional<RegistroSonoro> findTop1ByHablanteCodigoOrderByTimestampDesc(String codigo);
}
```

---

## Paso 4: Controller con 3 Endpoints GET [20%]

```java
package com.lenguas.controller;

import com.lenguas.model.Hablante;
import com.lenguas.model.RegistroSonoro;
import com.lenguas.repository.HablanteRepository;
import com.lenguas.repository.RegistroSonoroRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LenguasController {

    private final HablanteRepository hablanteRepository;
    private final RegistroSonoroRepository registroRepository;

    public LenguasController(HablanteRepository hablanteRepository,
                              RegistroSonoroRepository registroRepository) {
        this.hablanteRepository = hablanteRepository;
        this.registroRepository = registroRepository;
    }

    // Endpoint 1: Hablantes por clasificación de lengua
    // GET /api/hablantes/clasificacion/Critica
    @GetMapping("/hablantes/clasificacion/{clasificacion}")
    public List<Hablante> getHablantesPorClasificacion(@PathVariable String clasificacion) {
        return hablanteRepository.findByLenguaClasificacion(clasificacion);
    }

    // Endpoint 2: Registros sonoros de una lengua específica
    // GET /api/registros/lengua/Nasa Yuwe  (espacio → %20 en URL)
    @GetMapping("/registros/lengua/{nombre}")
    public List<RegistroSonoro> getRegistrosPorLengua(@PathVariable String nombre) {
        return registroRepository.findByHablanteLenguaNombre(nombre);
    }

    // Endpoint 3: Registro más reciente de un hablante por código
    // GET /api/registros/reciente/HAB001
    @GetMapping("/registros/reciente/{codigo}")
    public Optional<RegistroSonoro> getRegistroMasReciente(@PathVariable String codigo) {
        return registroRepository.findTop1ByHablanteCodigoOrderByTimestampDesc(codigo);
    }
}
```

---

## URLs para probar en el navegador

```
GET http://localhost:8080/api/hablantes/clasificacion/Critica
→ Espera: Pedro Ruiz (HAB004) y Lucia Vega (HAB005)

GET http://localhost:8080/api/registros/lengua/Nasa%20Yuwe
→ Espera: 4 registros de Ana Torres (HAB001) y Carlos Paz (HAB002)

GET http://localhost:8080/api/registros/reciente/HAB001
→ Espera: el registro 'Historia del origen' (timestamp '2024-03-20 09:00:00')
```

---

## Comparación directa: MetroCali → Lenguas

| MetroCali | Lenguas | Notas |
|---|---|---|
| `Ruta` | `Lengua` | Entidad raíz |
| `Bus` | `Hablante` | Entidad media |
| `GeoPoint` | `RegistroSonoro` | Entidad hoja con timestamp |
| `tipo` (de Ruta) | `clasificacion` (de Lengua) | Campo de filtro patrón 1 |
| `placa` (de Bus) | `codigo` (de Hablante) | Identificador único |
| `findByRutaTipo` | `findByLenguaClasificacion` | Patrón 1 |
| `findByBusRutaNombre` | `findByHablanteLenguaNombre` | Patrón 2 |
| `findTop1ByBusPlacaOrderByTimestampDesc` | `findTop1ByHablanteCodigoOrderByTimestampDesc` | Patrón 3 |
