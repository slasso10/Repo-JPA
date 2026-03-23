# Guía 5: Errores Comunes y Cómo Arreglarlos

> Errores que aparecen en el código inicial del examen y cómo identificarlos/solucionarlos.

---

## Errores en Entidades

### 1. Import de `javax` en vez de `jakarta`

```java
// ❌ Error (versiones antiguas de Spring)
import javax.persistence.Entity;
import javax.persistence.Id;

// ✅ Correcto (Spring Boot 3+)
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
// O simplemente:
import jakarta.persistence.*;
```

---

### 2. Falta `@Entity`

```java
// ❌ Error: Spring no sabe que es una tabla
public class Bus {
    @Id
    private Long id;
}

// ✅ Correcto
@Entity
public class Bus {
    @Id
    private Long id;
}
```

**Síntoma:** `Not a managed type: class com.example.Bus`

---

### 3. Falta constructor sin argumentos

```java
// ❌ Error: solo tiene constructor con parámetros
@Entity
public class Bus {
    public Bus(String placa) { ... }
}

// ✅ Correcto: añadir @NoArgsConstructor de Lombok O constructor explícito
@Entity
@NoArgsConstructor  // ← Lombok
public class Bus {
    public Bus(String placa) { ... }
}
```

**Síntoma:** `No default constructor for entity`

---

### 4. `mappedBy` incorrecto

```java
// ❌ Error: mappedBy usa el nombre de la CLASE, no del campo
@OneToMany(mappedBy = "Ruta")  // ← INCORRECTO

// ✅ Correcto: mappedBy usa el nombre del CAMPO en la otra entidad
@OneToMany(mappedBy = "ruta")  // ← nombre del campo @ManyToOne en Bus
```

**Síntoma:** `mappedBy reference an unknown target entity property`

---

### 5. FK faltante en la entidad hoja

```java
// ❌ Error: GeoPoint no tiene referencia a Bus
@Entity
public class GeoPoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double latitud;
    // ← falta la FK hacia Bus
}

// ✅ Correcto
@Entity
public class GeoPoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double latitud;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;  // ← FK hacia Bus
}
```

---

### 6. Tipo de dato incorrecto en la relación

```java
// ❌ Error: debería ser la entidad, no un ID
@ManyToOne
private Long rutaId;   // ← incorrecto, JPA espera la entidad

// ✅ Correcto
@ManyToOne
@JoinColumn(name = "ruta_id")
private Ruta ruta;     // ← la entidad directamente
```

---

### 7. `@GeneratedValue` faltante con IDs en data.sql

Si `data.sql` inserta IDs explícitos, hay que elegir la estrategia correcta:

```java
// Con IDs explícitos en data.sql, usar SEQUENCE o quitarla según la BD
// Para H2 con IDs explícitos:
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
// Funciona si data.sql incluye los IDs en el INSERT
```

---

## Errores en Query Methods

### 8. Nombre del campo mal escrito

```java
// ❌ Error: el campo en Bus se llama "ruta", no "route"
List<Bus> findByRouteTipo(String tipo);  // Spring no lo entiende

// ✅ Correcto: usar exactamente el nombre del campo Java
List<Bus> findByRutaTipo(String tipo);
```

**Síntoma:** `No property 'route' found for type 'Bus'`

---

### 9. Tipo de retorno incorrecto

```java
// ❌ Error: si puede haber varios resultados, no usar Object o entidad sola
Bus findByRutaTipo(String tipo);  // Error si hay múltiples resultados

// ✅ Correcto
List<Bus> findByRutaTipo(String tipo);   // para múltiples
Optional<Bus> findTop1By...(...);         // para uno solo (el más reciente)
```

---

### 10. Olvidar `Optional` en findTop1

```java
// ❌ Puede causar NullPointerException si no hay resultados
GeoPoint findTop1ByBusPlacaOrderByTimestampDesc(String placa);

// ✅ Correcto
Optional<GeoPoint> findTop1ByBusPlacaOrderByTimestampDesc(String placa);
```

---

## Errores en data.sql

### 11. Insertar hijos antes que padres

```sql
-- ❌ Error: bus_id=1 no existe todavía
INSERT INTO geo_point (id, latitud, bus_id) VALUES (1, 3.45, 1);
INSERT INTO bus (id, placa) VALUES (1, 'ABC');

-- ✅ Correcto: padre primero
INSERT INTO bus (id, placa) VALUES (1, 'ABC');
INSERT INTO geo_point (id, latitud, bus_id) VALUES (1, 3.45, 1);
```

**Síntoma:** `Referential integrity constraint violation`

---

### 12. Nombre de columna no coincide con la entidad

```sql
-- ❌ Error: la columna se llama "ruta_id" en la entidad pero data.sql usa "rutaId"
INSERT INTO bus (id, placa, rutaId) VALUES (1, 'ABC', 1);

-- ✅ Correcto: usar el nombre que JPA genera (snake_case por defecto)
INSERT INTO bus (id, placa, ruta_id) VALUES (1, 'ABC', 1);
```

---

## Errores al retornar JSON

### 13. Ciclo infinito en serialización

Cuando hay relaciones bidireccionales, Jackson entra en bucle:

```java
// ✅ Solución 1: @JsonIgnore en el lado @OneToMany
@JsonIgnore
@OneToMany(mappedBy = "ruta")
private List<Bus> buses;

// ✅ Solución 2: @JsonManagedReference / @JsonBackReference
// En Ruta:
@JsonManagedReference
@OneToMany(mappedBy = "ruta")
private List<Bus> buses;

// En Bus:
@JsonBackReference
@ManyToOne
@JoinColumn(name = "ruta_id")
private Ruta ruta;
```

**Síntoma:** `Could not write JSON: Infinite recursion (StackOverflowError)`

---

## Checklist antes de entregar

- [ ] Proyecto arranca sin errores en consola
- [ ] H2 Console muestra las tablas con datos correctos
- [ ] Los 3 endpoints GET responden con datos (no array vacío `[]`)
- [ ] El endpoint del "más reciente" devuelve un objeto (no `null`)
- [ ] No hay errores 500 al llamar los endpoints

## Cómo leer el stack trace

```
Caused by: org.hibernate.MappingException: Could not determine type for: ...
→ Problema en la relación (@ManyToOne o @OneToMany mal configurado)

Caused by: org.springframework.data.mapping.PropertyReferenceException: No property 'X' found for type 'Y'
→ Nombre de campo en query method no coincide con campo en la clase

Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
→ FK en data.sql apunta a un ID que no existe o inserción en orden incorrecto

org.springframework.beans.factory.BeanCreationException: ...No default constructor
→ Falta @NoArgsConstructor o constructor sin parámetros
```
