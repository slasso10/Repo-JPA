# Guía 4: Completar data.sql

> El examen entrega un `data.sql` incompleto. Hay que completarlo respetando las FK.

---

## Orden de inserción (CRÍTICO)

Siempre insertar en orden **padre → hijo**:

```
1. EntidadA (sin FK) → ej: Ruta
2. EntidadB (FK hacia A) → ej: Bus
3. EntidadC (FK hacia B) → ej: GeoPoint
```

Si insertas en orden incorrecto, obtienes error de constraint de FK.

---

## Plantilla data.sql — Modelo de 3 entidades

```sql
-- ═══════════════════════════════════════════════
-- 1. ENTIDAD PADRE (sin FK)
-- ═══════════════════════════════════════════════
INSERT INTO ruta (id, nombre, tipo) VALUES (1, 'T31', 'Troncal');
INSERT INTO ruta (id, nombre, tipo) VALUES (2, 'A10', 'Alimentador');
INSERT INTO ruta (id, nombre, tipo) VALUES (3, 'P05', 'Pretroncal');

-- ═══════════════════════════════════════════════
-- 2. ENTIDAD MEDIA (FK hacia padre)
-- ═══════════════════════════════════════════════
INSERT INTO bus (id, placa, ruta_id) VALUES (1, 'TXK001', 1);  -- bus de ruta T31
INSERT INTO bus (id, placa, ruta_id) VALUES (2, 'TXK002', 1);  -- bus de ruta T31
INSERT INTO bus (id, placa, ruta_id) VALUES (3, 'ALM001', 2);  -- bus de ruta A10

-- ═══════════════════════════════════════════════
-- 3. ENTIDAD HOJA (FK hacia media, con timestamp)
-- ═══════════════════════════════════════════════
INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (1, 3.4516, -76.5320, '2024-01-15 08:00:00', 1);

INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (2, 3.4520, -76.5315, '2024-01-15 08:05:00', 1);  -- más reciente para bus 1

INSERT INTO geo_point (id, latitud, longitud, timestamp, bus_id)
VALUES (3, 3.4480, -76.5400, '2024-01-15 08:10:00', 2);
```

---

## Modelo Lenguas en Peligro

```sql
-- Lenguas
INSERT INTO lengua (id, nombre, clasificacion) VALUES (1, 'Nasa Yuwe', 'Amenazada');
INSERT INTO lengua (id, nombre, clasificacion) VALUES (2, 'Wayuunaiki', 'Vigente');
INSERT INTO lengua (id, nombre, clasificacion) VALUES (3, 'Cubeo', 'Critica');

-- Hablantes (FK hacia lengua)
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (1, 'Ana Torres', 'HAB001', 1);
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (2, 'Carlos Paz', 'HAB002', 1);
INSERT INTO hablante (id, nombre, codigo, lengua_id) VALUES (3, 'María Luna', 'HAB003', 3);

-- Registros Sonoros (FK hacia hablante, con timestamp)
INSERT INTO registro_sonoro (id, descripcion, timestamp, hablante_id)
VALUES (1, 'Canción de bienvenida', '2024-03-01 10:00:00', 1);

INSERT INTO registro_sonoro (id, descripcion, timestamp, hablante_id)
VALUES (2, 'Frase cotidiana', '2024-03-15 14:30:00', 1);  -- más reciente de HAB001

INSERT INTO registro_sonoro (id, descripcion, timestamp, hablante_id)
VALUES (3, 'Narración tradicional', '2024-02-20 09:00:00', 3);
```

---

## Cómo deducir los nombres de las columnas

Si el `data.sql` tiene errores o está incompleto, mira las entidades Java:

```java
@Column(name = "ruta_id")   // ← nombre de columna explícito
private Ruta ruta;

private String placa;       // ← sin @Column: nombre = "placa"
```

Si no hay `@Table(name=...)`, el nombre de la tabla es el nombre de la clase en minúsculas.

---

## Verificar datos con H2 Console

Con `spring.h2.console.enabled=true`:

1. Ir a `http://localhost:8080/h2-console`
2. URL: `jdbc:h2:mem:testdb` (o la configurada)
3. Ejecutar queries para verificar:

```sql
SELECT * FROM ruta;
SELECT * FROM bus;
SELECT b.*, r.nombre as ruta_nombre FROM bus b JOIN ruta r ON b.ruta_id = r.id;
SELECT g.*, b.placa FROM geo_point g JOIN bus b ON g.bus_id = b.id ORDER BY g.timestamp DESC;
```
