# 📚 Repo de Referencia — Spring Boot + JPA
**Computación en Internet II — ICESI**

---

## ¿Qué hay aquí?

| Archivo | Qué resuelve |
|---|---|
| [`guias/01-entidades-y-relaciones.md`](guias/01-entidades-y-relaciones.md) | Cómo corregir entidades JPA con errores (20% del parcial) |
| [`guias/02-query-methods.md`](guias/02-query-methods.md) | Cómo escribir cualquier query method (60% del parcial) |
| [`guias/03-controller-endpoints.md`](guias/03-controller-endpoints.md) | Cómo hacer los endpoints GET (20% del parcial) |
| [`guias/04-data-sql.md`](guias/04-data-sql.md) | Cómo completar data.sql correctamente |
| [`guias/05-errores-comunes.md`](guias/05-errores-comunes.md) | Errores típicos que ponen en el examen y cómo arreglarlos |
| [`plantillas/entidad.java`](plantillas/entidad.java) | Plantilla lista para copiar-pegar |
| [`plantillas/repository.java`](plantillas/repository.java) | Plantilla de repositorio con query methods |
| [`plantillas/controller.java`](plantillas/controller.java) | Plantilla de controller con 3 endpoints GET |
| [`ejemplos-examenes/metrocali.md`](ejemplos-examenes/metrocali.md) | Solución completa del examen MetroCali |
| [`ejemplos-examenes/lenguas.md`](ejemplos-examenes/lenguas.md) | Solución completa del examen Lenguas en Peligro |

---

## Patrón del parcial (siempre es igual)

El examen **siempre** sigue esta estructura:

```
1. [20%] Corregir errores en las entidades + hacer correr data.sql
2. [20%] Query method 1 — buscar por relación/campo de entidad relacionada
3. [20%] Query method 2 — obtener colección cruzando relaciones
4. [20%] Query method 3 — obtener el más reciente (Top1, OrderBy...Desc)
5. [20%] 3 endpoints GET que prueben los 3 query methods anteriores
```

**Empieza siempre por las entidades.** Si las entidades están rotas, nada más funciona.
