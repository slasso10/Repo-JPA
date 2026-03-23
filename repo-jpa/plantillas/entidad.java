// ═══════════════════════════════════════════════════════════
// PLANTILLA: Entidad JPA — copiar y adaptar nombres
// ═══════════════════════════════════════════════════════════
package com.ejemplo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ───────────────────────────────────────────
// ENTIDAD A: La raíz (sin FK hacia otras)
// Ejemplo: Ruta, Lengua, Categoria
// ───────────────────────────────────────────
@Data
@NoArgsConstructor
@Entity
@Table(name = "ruta")  // ← CAMBIAR al nombre real de la tabla en data.sql
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String tipo;   // ← campo por el que suelen filtrar (Patrón 1)

    // Relación hacia los hijos — @JsonIgnore evita ciclo infinito en JSON
    @JsonIgnore
    @OneToMany(mappedBy = "ruta")  // ← "ruta" = nombre del campo @ManyToOne en Bus
    private List<Bus> buses = new ArrayList<>();
}


// ───────────────────────────────────────────
// ENTIDAD B: La del medio (FK hacia A)
// Ejemplo: Bus, Hablante, Empleado
// ───────────────────────────────────────────
@Data
@NoArgsConstructor
@Entity
@Table(name = "bus")  // ← CAMBIAR
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;  // ← identificador único (nombre, código, matrícula)

    // FK hacia la entidad padre
    @ManyToOne
    @JoinColumn(name = "ruta_id")  // ← CAMBIAR: nombre de la columna FK en esta tabla
    private Ruta ruta;

    // Relación hacia los hijos
    @JsonIgnore
    @OneToMany(mappedBy = "bus")  // ← "bus" = nombre del campo @ManyToOne en GeoPoint
    private List<GeoPoint> geoPoints = new ArrayList<>();
}


// ───────────────────────────────────────────
// ENTIDAD C: La hoja (FK hacia B, con timestamp)
// Ejemplo: GeoPoint, RegistroSonoro, Evento
// ───────────────────────────────────────────
@Data
@NoArgsConstructor
@Entity
@Table(name = "geo_point")  // ← CAMBIAR
public class GeoPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitud;
    private Double longitud;

    private LocalDateTime timestamp;  // ← para consultas de "más reciente"

    // FK hacia la entidad media
    @ManyToOne
    @JoinColumn(name = "bus_id")  // ← CAMBIAR
    private Bus bus;
}
