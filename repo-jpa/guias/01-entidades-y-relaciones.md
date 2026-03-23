# Guía 1: Entidades JPA y Relaciones

> **Objetivo del examen:** Corregir errores en las entidades hasta que el proyecto corra con `data.sql`.

---

## Anatomía de una entidad correcta

```java
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                          // ← Lombok: genera getters, setters, toString, equals
@NoArgsConstructor             // ← JPA REQUIERE constructor sin argumentos
@Entity                        // ← Le dice a JPA que esta clase = tabla en BD
@Table(name = "nombre_tabla")  // ← Opcional: nombre de la tabla (si no se pone, usa el nombre de la clase)
public class MiEntidad {

    @Id                                                    // ← OBLIGATORIO: clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // ← Auto-incremento
    private Long id;

    @Column(name = "nombre_columna", nullable = false)    // ← Opcional: configurar columna
    private String nombre;

    private String campoSimple;   // ← Sin anotación = columna con mismo nombre
}
```

### Checklist de errores comunes en el examen

- [ ] `@Entity` faltante o mal escrito
- [ ] `@Id` faltante → error al arrancar
- [ ] `@GeneratedValue` faltante → IDs no se generan
- [ ] Constructor sin argumentos faltante → JPA no puede instanciar la clase
- [ ] Import equivocado (`javax.persistence` en vez de `jakarta.persistence`)
- [ ] `@Column(nullable = false)` en un campo que sí puede ser null en `data.sql`
- [ ] Nombre de tabla/columna no coincide con `data.sql`
- [ ] Relación con tipo de dato equivocado (ej: `Long` donde debería ser `List<OtraEntidad>`)

---

## Relaciones — Los 4 tipos

### @ManyToOne (muchos a uno) — el más común

Usado cuando **muchos** de esta entidad pertenecen a **uno** de la otra.

**Ejemplo:** Muchos `Bus` → una `Ruta`

```java
// En la entidad Bus (el lado "muchos")
@ManyToOne
@JoinColumn(name = "ruta_id")   // ← columna FK en la tabla bus
private Ruta ruta;
```

```sql
-- En data.sql, se insertan pasando el ID:
INSERT INTO bus (id, placa, ruta_id) VALUES (1, 'ABC123', 1);
```

---

### @OneToMany (uno a muchos) — el lado inverso

Usado en la entidad "padre" para acceder a la lista de hijos.

**Ejemplo:** Una `Ruta` tiene muchos `Bus`

```java
// En la entidad Ruta (el lado "uno")
@OneToMany(mappedBy = "ruta")   // ← "ruta" = nombre del campo @ManyToOne en Bus
private List<Bus> buses = new ArrayList<>();
```

> ⚠️ `mappedBy` = el nombre del **campo** en la otra entidad, NO el nombre de la clase ni de la tabla.

---

### @OneToOne (uno a uno)

```java
// En la entidad dueña (la que tiene la FK)
@OneToOne
@JoinColumn(name = "detalle_id")
private Detalle detalle;

// En la entidad inversa (opcional, para navegación bidireccional)
@OneToOne(mappedBy = "detalle")
private Entidad entidad;
```

---

### @ManyToMany (muchos a muchos)

```java
// En la entidad dueña
@ManyToMany
@JoinTable(
    name = "estudiante_curso",                         // tabla intermedia
    joinColumns = @JoinColumn(name = "estudiante_id"), // FK hacia esta entidad
    inverseJoinColumns = @JoinColumn(name = "curso_id") // FK hacia la otra entidad
)
private List<Curso> cursos = new ArrayList<>();

// En la entidad inversa
@ManyToMany(mappedBy = "cursos")
private List<Estudiante> estudiantes = new ArrayList<>();
```

---

## Modelo típico del examen (3 entidades)

El examen siempre tiene **3 entidades** con esta estructura:

```
EntidadA (ej: Ruta, Lengua)
    |
    | @OneToMany
    |
EntidadB (ej: Bus, Hablante)  ← tiene @ManyToOne hacia A
    |
    | @OneToMany
    |
EntidadC (ej: GeoPoint, RegistroSonoro)  ← tiene @ManyToOne hacia B, tiene timestamp
```

### Plantilla para EntidadA (la raíz)

```java
@Data
@NoArgsConstructor
@Entity
@Table(name = "ruta")  // ajustar nombre
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo;  // campo por el que suelen filtrar en el examen

    @OneToMany(mappedBy = "ruta")
    private List<Bus> buses = new ArrayList<>();
}
```

### Plantilla para EntidadB (la del medio)

```java
@Data
@NoArgsConstructor
@Entity
@Table(name = "bus")  // ajustar nombre
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;  // identificador único por el que suelen buscar

    @ManyToOne
    @JoinColumn(name = "ruta_id")  // ajustar nombre FK
    private Ruta ruta;

    @OneToMany(mappedBy = "bus")
    private List<GeoPoint> geoPoints = new ArrayList<>();
}
```

### Plantilla para EntidadC (la hoja, con timestamp)

```java
@Data
@NoArgsConstructor
@Entity
@Table(name = "geo_point")  // ajustar nombre
public class GeoPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitud;
    private Double longitud;

    private LocalDateTime timestamp;  // ← IMPORTANTE para las consultas de "más reciente"

    @ManyToOne
    @JoinColumn(name = "bus_id")  // ajustar nombre FK
    private Bus bus;
}
```

---

## ¿Por qué no arranca el proyecto?

Si el proyecto no arranca, revisa en orden:

1. **¿Hay error de import?** → Cambiar `javax.persistence.*` por `jakarta.persistence.*`
2. **¿Falta `@Entity`?** → La clase no se mapea a tabla
3. **¿Falta `@Id`?** → `IdentifierGenerationException`
4. **¿`mappedBy` incorrecto?** → `MappingException: Could not determine type for...`
5. **¿FK en data.sql apunta a ID que no existe?** → Error de constraint al insertar datos
6. **¿Nombre de tabla en `@Table` no coincide con INSERT en data.sql?** → `Table not found`

---

## application.properties recomendado para el examen

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Carga data.sql automáticamente
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# Ver las queries en consola (útil para depurar)
spring.jpa.show-sql=true

# Consola H2 (para verificar datos)
spring.h2.console.enabled=true
```
