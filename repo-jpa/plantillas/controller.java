// ═══════════════════════════════════════════════════════════
// PLANTILLA: Controller con los 3 endpoints GET del examen
// ═══════════════════════════════════════════════════════════
package com.ejemplo.controller;

import com.ejemplo.model.Bus;
import com.ejemplo.model.GeoPoint;
import com.ejemplo.repository.BusRepository;
import com.ejemplo.repository.GeoPointRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MiController {

    // ── Inyección de repositorios (sin service, como permite el examen) ──
    private final BusRepository busRepository;
    private final GeoPointRepository geoPointRepository;

    public MiController(BusRepository busRepository,
                        GeoPointRepository geoPointRepository) {
        this.busRepository = busRepository;
        this.geoPointRepository = geoPointRepository;
    }

    // ── ENDPOINT 1 ──────────────────────────────────────────
    // Patrón 1: Buscar entidad B por campo de entidad A
    // URL: GET /api/buses/tipo/Alimentador
    @GetMapping("/buses/tipo/{tipo}")
    public List<Bus> getBusesPorTipo(@PathVariable String tipo) {
        return busRepository.findByRutaTipo(tipo);
    }

    // ── ENDPOINT 2 ──────────────────────────────────────────
    // Patrón 2: Obtener entidad C filtrando por nombre de entidad A
    // URL: GET /api/geopoints/ruta/T31
    @GetMapping("/geopoints/ruta/{nombre}")
    public List<GeoPoint> getGeopointsPorRuta(@PathVariable String nombre) {
        return geoPointRepository.findByBusRutaNombre(nombre);
    }

    // ── ENDPOINT 3 ──────────────────────────────────────────
    // Patrón 3: El más reciente de entidad C para un identificador de B
    // URL: GET /api/geopoints/reciente/TXK001
    @GetMapping("/geopoints/reciente/{placa}")
    public Optional<GeoPoint> getUltimaUbicacion(@PathVariable String placa) {
        return geoPointRepository.findTop1ByBusPlacaOrderByTimestampDesc(placa);
    }
}


// ═══════════════════════════════════════════════════════════
// VERSIÓN PARA LENGUAS EN PELIGRO
// (descomentar y usar si el contexto es ese)
// ═══════════════════════════════════════════════════════════

/*
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
    // URL: GET /api/hablantes/clasificacion/Critica
    @GetMapping("/hablantes/clasificacion/{clasificacion}")
    public List<Hablante> getHablantesPorClasificacion(@PathVariable String clasificacion) {
        return hablanteRepository.findByLenguaClasificacion(clasificacion);
    }

    // Endpoint 2: Registros sonoros de una lengua específica
    // URL: GET /api/registros/lengua/Nasa%20Yuwe
    @GetMapping("/registros/lengua/{nombre}")
    public List<RegistroSonoro> getRegistrosPorLengua(@PathVariable String nombre) {
        return registroRepository.findByHablanteLenguaNombre(nombre);
    }

    // Endpoint 3: Registro más reciente de un hablante por código
    // URL: GET /api/registros/reciente/HAB001
    @GetMapping("/registros/reciente/{codigo}")
    public Optional<RegistroSonoro> getRegistroMasReciente(@PathVariable String codigo) {
        return registroRepository.findTop1ByHablanteCodigoOrderByTimestampDesc(codigo);
    }
}
*/
