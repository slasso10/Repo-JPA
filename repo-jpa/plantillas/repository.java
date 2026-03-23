// ═══════════════════════════════════════════════════════════
// PLANTILLA: Repositorios JPA con los 3 patrones del examen
// ═══════════════════════════════════════════════════════════
package com.ejemplo.repository;

import com.ejemplo.model.Bus;
import com.ejemplo.model.GeoPoint;
import com.ejemplo.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


// ───────────────────────────────────────────
// Repositorio de la Entidad A (la raíz)
// ───────────────────────────────────────────
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    // Métodos heredados: save, findById, findAll, deleteById, count, existsById
    // Normalmente no necesitas agregar nada aquí
}


// ───────────────────────────────────────────
// Repositorio de la Entidad B (la del medio)
// ───────────────────────────────────────────
public interface BusRepository extends JpaRepository<Bus, Long> {

    // ── PATRÓN 1 ─────────────────────────────────────────────
    // Buscar buses por campo de su entidad relacionada (Ruta)
    // Enunciado: "Encontrar los buses que atienden rutas de un tipo particular"
    //
    // Estructura: findBy + [campoRelacion] + [campoEnRelacion]
    //             findBy + Ruta           + Tipo
    List<Bus> findByRutaTipo(String tipo);

    // Variante si el campo es "nombre":
    List<Bus> findByRutaNombre(String nombre);

    // Alternativa con @Query (JPQL):
    @Query("SELECT b FROM Bus b WHERE b.ruta.tipo = :tipo")
    List<Bus> findBusesPorTipoRuta(@Param("tipo") String tipo);
}


// ───────────────────────────────────────────
// Repositorio de la Entidad C (la hoja)
// ───────────────────────────────────────────
public interface GeoPointRepository extends JpaRepository<GeoPoint, Long> {

    // ── PATRÓN 2 ─────────────────────────────────────────────
    // Obtener colección cruzando dos relaciones
    // Enunciado: "Obtener todos los GeoPoints de buses que atienden una ruta específica"
    //
    // Estructura: findBy + [campoRelacion1] + [campoRelacion2] + [campo]
    //             findBy + Bus              + Ruta              + Nombre
    List<GeoPoint> findByBusRutaNombre(String nombreRuta);

    // Si el identificador de la ruta es "tipo":
    List<GeoPoint> findByBusRutaTipo(String tipo);

    // ── PATRÓN 3 ─────────────────────────────────────────────
    // Obtener el más reciente de una entidad por su identificador
    // Enunciado: "Obtener la ubicación más reciente de un bus según su matrícula/placa"
    //
    // Estructura: findTop1By + [campoRelacion] + [identificador] + OrderBy + [timestamp] + Desc
    //             findTop1By + Bus             + Placa           + OrderBy + Timestamp   + Desc
    Optional<GeoPoint> findTop1ByBusPlacaOrderByTimestampDesc(String placa);

    // Si el identificador se llama "matricula":
    Optional<GeoPoint> findTop1ByBusMatriculaOrderByTimestampDesc(String matricula);

    // Alternativa con @Query:
    @Query("SELECT g FROM GeoPoint g WHERE g.bus.placa = :placa ORDER BY g.timestamp DESC LIMIT 1")
    Optional<GeoPoint> findUltimaUbicacion(@Param("placa") String placa);

    // ── PATRÓN EXTRA: Recorrido completo (ordenado) ───────────
    // Enunciado: "Obtener el recorrido de un bus por placa, ordenado por tiempo"
    List<GeoPoint> findByBusPlacaOrderByTimestampAsc(String placa);
}


// ═══════════════════════════════════════════════════════════
// GUÍA DE SUSTITUCIÓN — Para adaptar al nuevo contexto:
//
// Si el modelo es Lengua → Hablante → RegistroSonoro:
//
//   BusRepository     → HablanteRepository
//   GeoPointRepository → RegistroSonoroRepository
//   Ruta              → Lengua
//   Bus               → Hablante
//   GeoPoint          → RegistroSonoro
//   placa             → codigo (o nombre, según enunciado)
//   tipo (de Ruta)    → clasificacion (de Lengua)
//
//   Ejemplos:
//   findByLenguaClasificacion(String clasificacion)
//   findByHablanteLenguaNombre(String nombreLengua)
//   findTop1ByHablanteCodigoOrderByTimestampDesc(String codigo)
// ═══════════════════════════════════════════════════════════
