# Solución Completa — Examen MetroCali

**Contexto:** MetroCali gestiona Rutas, Buses y GeoPoints.
- Una Ruta → muchos Buses
- Un Bus → muchos GeoPoints (con timestamp y latitud/longitud)

---

## Paso 1: Corregir las Entidades [20%]

### Ruta.java
```java
package com.metrocali.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ruta")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo;  // Troncal, Alimentador, Pretroncal

    @JsonIgnore
    @OneToMany(mappedBy = "ruta")
    private List<Bus> buses = new ArrayList<>();
}
```

### Bus.java
```java
package com.metrocali.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "bus")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;

    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @JsonIgnore
    @OneToMany(mappedBy = "bus")
    private List<GeoPoint> geoPoints = new ArrayList<>();
}
```

### GeoPoint.java
```java
package com.metrocali.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "geo_point")
public class GeoPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitud;
    private Double longitud;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;
}
```

---

## Paso 2: data.sql completo

```sql
-- Rutas
INSERT INTO ruta (id, nombre, tipo) VALUES (1, 'T31', 'Troncal');
INSERT INTO ruta (id, nombre, tipo) VALUES (2, 'T45', 'Troncal');
INSERT INTO ruta (id, nombre, tipo) VALUES (3, 'A10', 'Alimentador');
INSERT INTO ruta (id, nombre, tipo) VALUES (4, 'A22', 'Alimentador');

-- Buses (ruta_id referencia a ruta.id)
INSERT INTO bus (id, placa, ruta_id) VALUES (1, 'TXK001', 1);
INSERT INTO bus (id, placa, ruta_id) VALUES (2, 'TXK002', 1);
INSERT INTO bus (id, placa, ruta_id) VALUES (3, 'TXK003', 2);
INSERT INTO bus (id, placa, ruta_id) VALUES (4, 'ALM001', 3);
INSERT INTO bus (id, placa, ruta_id) VALUES (5, 'ALM002', 4);

-- GeoPoints (bus_id referencia a bus.id)
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (1, 3.4516, -76.5320, '2024-01-15 08:00:00', 1);
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (2, 3.4520, -76.5315, '2024-01-15 08:05:00', 1);
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (3, 3.4525, -76.5310, '2024-01-15 08:10:00', 1);
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (4, 3.4480, -76.5400, '2024-01-15 08:00:00', 2);
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (5, 3.4490, -76.5390, '2024-01-15 08:07:00', 2);
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (6, 3.4600, -76.5200, '2024-01-15 09:00:00', 4);
```

---

## Paso 3: Repositorios con Query Methods [60%]

### RutaRepository.java
```java
package com.metrocali.repository;

import com.metrocali.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
}
```

### BusRepository.java
```java
package com.metrocali.repository;

import com.metrocali.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {

    // [20%] Encontrar buses que atienden una ruta específica por nombre de la ruta
    List<Bus> findByRutaNombre(String nombre);

    // (Variante del examen B) Buses que atienden rutas de un tipo
    List<Bus> findByRutaTipo(String tipo);
}
```

### GeoPointRepository.java
```java
package com.metrocali.repository;

import com.metrocali.model.GeoPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GeoPointRepository extends JpaRepository<GeoPoint, Long> {

    // [20%] Obtener el recorrido de un bus por placa (todos los geopoints ordenados)
    List<GeoPoint> findByBusPlacaOrderByTimestampAsc(String placa);

    // (Variante) Todos los geopoints de buses que atienden una ruta específica
    List<GeoPoint> findByBusRutaNombre(String nombreRuta);

    // [20%] Obtener la ubicación más reciente de un bus según su placa/matrícula
    Optional<GeoPoint> findTop1ByBusPlacaOrderByTimestampDesc(String placa);
}
```

---

## Paso 4: Controller con 3 Endpoints GET [20%]

```java
package com.metrocali.controller;

import com.metrocali.model.Bus;
import com.metrocali.model.GeoPoint;
import com.metrocali.repository.BusRepository;
import com.metrocali.repository.GeoPointRepository;
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

    // Endpoint 1: Buses de una ruta por nombre
    // GET /api/buses/ruta/T31
    @GetMapping("/buses/ruta/{nombre}")
    public List<Bus> getBusesPorRuta(@PathVariable String nombre) {
        return busRepository.findByRutaNombre(nombre);
    }

    // Endpoint 2: Recorrido completo de un bus (todos los geopoints ordenados)
    // GET /api/recorrido/TXK001
    @GetMapping("/recorrido/{placa}")
    public List<GeoPoint> getRecorrido(@PathVariable String placa) {
        return geoPointRepository.findByBusPlacaOrderByTimestampAsc(placa);
    }

    // Endpoint 3: Ubicación más reciente de un bus
    // GET /api/ubicacion/TXK001
    @GetMapping("/ubicacion/{placa}")
    public Optional<GeoPoint> getUltimaUbicacion(@PathVariable String placa) {
        return geoPointRepository.findTop1ByBusPlacaOrderByTimestampDesc(placa);
    }
}
```

---

## URLs para probar en el navegador

```
GET http://localhost:8080/api/buses/ruta/T31
→ Espera: lista de buses con placa TXK001 y TXK002

GET http://localhost:8080/api/recorrido/TXK001
→ Espera: 3 geopoints ordenados por timestamp

GET http://localhost:8080/api/ubicacion/TXK001
→ Espera: el geopoint con timestamp '2024-01-15 08:10:00'
```
