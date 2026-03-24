# Guía 6: 50 Ejercicios de Query Methods

## Modelo de referencia

Estas son las entidades que usan todos los ejercicios. Tenlas presentes para entender las relaciones.

```java
@Entity @Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}

@Entity @Table(name = "instructors")
public class Instructor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private Timestamp hireDate;
    private boolean active;

    @ManyToOne
    private Department department;
}

@Entity @Table(name = "courses")
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;       // e.g. CS101
    private String title;
    private int credits;
    private Timestamp startDate;
    private boolean active;

    @Enumerated(EnumType.STRING)
    private Level level;       // BEGINNER / INTERMEDIATE / ADVANCED

    @ManyToOne
    private Department department;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}

@Entity @Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Integer age;
    private Double gpa;
    private boolean active;
    private Timestamp registrationDate;
    private String city;
    private String state;

    @ManyToMany
    @JoinTable(name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();

    @ManyToOne
    private Instructor advisor;

    @OneToMany(mappedBy = "student")
    private List<Enrollment> enrollments = new ArrayList<>();
}

@Entity @Table(name = "enrollments")
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private Student student;
    @ManyToOne private Course course;

    @Enumerated(EnumType.STRING)
    private Semester semester;         // SPRING, FALL, SUMMER, WINTER

    private Double grade;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;   // ENROLLED, DROPPED, COMPLETED
}

public enum Level          { BEGINNER, INTERMEDIATE, ADVANCED }
public enum Semester       { SPRING, SUMMER, FALL, WINTER }
public enum EnrollmentStatus { ENROLLED, DROPPED, COMPLETED }
```

---

## Mapa de relaciones

```
Department
    ↑ @ManyToOne
Instructor ←── @ManyToOne ── Student ──@ManyToMany──► Course
                                │                        ↑
                                │ @OneToMany             │ @ManyToOne
                                ▼                        │
                            Enrollment ─────────────────┘
```

---

## Los 50 ejercicios

### 1. `findByUsername`
Encuentra todos los estudiantes cuyo `username` sea igual a un string dado.
```java
List<Student> findByUsername(String username);
```

---

### 2. `findByEmailIgnoreCase`
Busca estudiantes por email ignorando mayúsculas/minúsculas.
```java
List<Student> findByEmailIgnoreCase(String email);
// Encuentra "ALICE@EXAMPLE.COM" aunque esté guardado como "alice@example.com"
```

---

### 3. `findByFirstNameAndLastName`
Busca estudiantes por `firstName` Y `lastName` (ambos deben coincidir).
```java
List<Student> findByFirstNameAndLastName(String firstName, String lastName);
// repo.findByFirstNameAndLastName("Alice", "Lopez");
```

---

### 4. `findByAgeGreaterThan`
Encuentra estudiantes con `age` estrictamente mayor a un valor.
```java
List<Student> findByAgeGreaterThan(Integer age);
// repo.findByAgeGreaterThan(21) → edad > 21
```

---

### 5. `findByAgeGreaterThanEqual`
Encuentra estudiantes con `age` mayor O igual a un valor.
```java
List<Student> findByAgeGreaterThanEqual(Integer age);
// repo.findByAgeGreaterThanEqual(21) → edad >= 21
```

---

### 6. `findByGpaBetween`
Encuentra estudiantes con `gpa` dentro de un rango inclusivo.
```java
List<Student> findByGpaBetween(Double min, Double max);
// repo.findByGpaBetween(3.0, 4.0) → 3.0 <= gpa <= 4.0
```

---

### 7. `findByActiveTrue`
Encuentra estudiantes activos (campo boolean `active` = true).
```java
List<Student> findByActiveTrue();
```

---

### 8. `findByActiveFalse`
Encuentra estudiantes inactivos (campo boolean `active` = false).
```java
List<Student> findByActiveFalse();
```

---

### 9. `findByRegistrationDateAfter`
Encuentra estudiantes que se registraron DESPUÉS de una fecha dada.
```java
List<Student> findByRegistrationDateAfter(Timestamp date);
// repo.findByRegistrationDateAfter(Timestamp.valueOf("2024-01-01 00:00:00"));
```

---

### 10. `findByRegistrationDateBetween`
Encuentra estudiantes registrados ENTRE dos fechas.
```java
List<Student> findByRegistrationDateBetween(Timestamp from, Timestamp to);
```

---

### 11. `findByCoursesCode`
Encuentra estudiantes inscritos en un curso con un código dado (join implícito con colección `courses`).
```java
List<Student> findByCoursesCode(String code);
// repo.findByCoursesCode("CS101");
```

---

### 12. `findByCoursesTitleContainingIgnoreCase`
Busca estudiantes que tengan cursos cuyo título CONTENGA una subcadena (sin importar mayúsculas).
```java
List<Student> findByCoursesTitleContainingIgnoreCase(String titlePart);
// repo.findByCoursesTitleContainingIgnoreCase("data");
```

---

### 13. `findDistinctByCoursesCode`
Encuentra estudiantes distintos (sin duplicados) inscritos en el curso con ese código.
```java
List<Student> findDistinctByCoursesCode(String code);
// Sin Distinct, un estudiante con 2 enrollments en el mismo curso aparecería dos veces
```

---

### 14. `findByAdvisorLastName`
Encuentra estudiantes cuyo advisor (relación `@ManyToOne` con `Instructor`) tiene un apellido dado.
```java
List<Student> findByAdvisorLastName(String lastName);
// repo.findByAdvisorLastName("Gonzalez");
```

---

### 15. `findByAdvisorId`
Encuentra estudiantes por el ID del advisor.
```java
List<Student> findByAdvisorId(Long instructorId);
// repo.findByAdvisorId(42L);
```

---

### 16. `findByAdvisorIsNull`
Encuentra estudiantes que NO tienen advisor asignado.
```java
List<Student> findByAdvisorIsNull();
```

---

### 17. `existsByEmail`
Comprueba si existe un estudiante con ese email. Devuelve `boolean`, no lista.
```java
boolean existsByEmail(String email);
// boolean exists = repo.existsByEmail("alice@example.com");
```

---

### 18. `countByActiveTrue`
Cuenta cuántos estudiantes están activos. Devuelve `long`.
```java
long countByActiveTrue();
// long n = repo.countByActiveTrue();
```

---

### 19. `deleteByUsername`
Borra estudiantes por `username`.
```java
void deleteByUsername(String username);
// repo.deleteByUsername("old_user");
```
> ⚠️ Requiere `@Transactional` en el método que lo llame.

---

### 20. `findTop5ByOrderByGpaDesc`
Devuelve los 5 estudiantes con MAYOR GPA.
```java
List<Student> findTop5ByOrderByGpaDesc();
// Sin filtro (no hay "By" con condición), solo ordena y toma los primeros 5
```

---

### 21. `findFirstByOrderByRegistrationDateAsc`
Encuentra el primer estudiante registrado (el más antiguo).
```java
Optional<Student> findFirstByOrderByRegistrationDateAsc();
```

---

### 22. `findByFirstNameStartingWith`
Busca estudiantes cuyo `firstName` EMPIEZA con un prefijo.
```java
List<Student> findByFirstNameStartingWith(String prefix);
// repo.findByFirstNameStartingWith("Al") → "Alice", "Alberto", "Alejandro"...
```

---

### 23. `findByLastNameEndingWith`
Busca estudiantes cuyo `lastName` TERMINA con un sufijo.
```java
List<Student> findByLastNameEndingWith(String suffix);
// repo.findByLastNameEndingWith("ez") → "Lopez", "Gomez", "Perez"...
```

---

### 24. `findByFirstNameContaining`
Busca estudiantes cuyo `firstName` CONTIENE una subcadena.
```java
List<Student> findByFirstNameContaining(String fragment);
// repo.findByFirstNameContaining("li") → "Alice", "Olivia"...
```

---

### 25. `findByEmailLike`
Busca estudiantes con `email` usando patrón LIKE (usa `%` para comodines).
```java
List<Student> findByEmailLike(String pattern);
// repo.findByEmailLike("%@gmail.com") → emails que terminan en @gmail.com
```

---

### 26. `findByGpaIsNull`
Encuentra estudiantes cuyo `gpa` es NULL.
```java
List<Student> findByGpaIsNull();
```

---

### 27. `findByGpaIsNotNull`
Encuentra estudiantes cuyo `gpa` NO es NULL.
```java
List<Student> findByGpaIsNotNull();
```

---

### 28. `findByUsernameIn`
Encuentra estudiantes cuyo `username` está DENTRO de una colección dada.
```java
List<Student> findByUsernameIn(Collection<String> usernames);
// repo.findByUsernameIn(List.of("alice", "bob", "carlos"));
```

---

### 29. `findByUsernameNotIn`
Encuentra estudiantes cuyo `username` NO está en una colección dada.
```java
List<Student> findByUsernameNotIn(Collection<String> usernames);
```

---

### 30. `findByUsernameNot`
Encuentra estudiantes cuyo `username` NO sea el dado.
```java
List<Student> findByUsernameNot(String username);
```

---

### 31. `findByDepartmentName` (en CourseRepository)
Busca cursos por el nombre del departamento asociado.
```java
// En CourseRepository:
List<Course> findByDepartmentName(String deptName);
// courseRepo.findByDepartmentName("Computer Science");
```

---

### 32. `findByCreditsLessThan` (en CourseRepository)
Encuentra cursos con MENOS de N créditos.
```java
List<Course> findByCreditsLessThan(int credits);
// courseRepo.findByCreditsLessThan(4) → cursos con 1, 2 o 3 créditos
```

---

### 33. `findByLevelIn` (en CourseRepository)
Encuentra cursos cuyo `level` está dentro de una colección de niveles (Enum).
```java
List<Course> findByLevelIn(Collection<Level> levels);
// courseRepo.findByLevelIn(List.of(Level.BEGINNER, Level.INTERMEDIATE));
```

---

### 34. `findByEnrollmentsGradeGreaterThan`
Encuentra estudiantes que tengan ALGUNA inscripción con nota mayor a X.
```java
List<Student> findByEnrollmentsGradeGreaterThan(Double grade);
// repo.findByEnrollmentsGradeGreaterThan(85.0);
```

---

### 35. `findByEnrollmentsStatus`
Encuentra estudiantes según el estado de alguna de sus inscripciones (Enum).
```java
List<Student> findByEnrollmentsStatus(EnrollmentStatus status);
// repo.findByEnrollmentsStatus(EnrollmentStatus.COMPLETED);
```

---

### 36. `findByEnrollmentsSemesterAndEnrollmentsCourseCode`
Encuentra estudiantes inscritos en un curso específico durante un semestre concreto.
```java
List<Student> findByEnrollmentsSemesterAndEnrollmentsCourseCode(
    Semester semester, String courseCode
);
// repo.findByEnrollmentsSemesterAndEnrollmentsCourseCode(Semester.FALL, "CS101");
```

---

### 37. `findByCoursesLevelAndCoursesCreditsGreaterThan`
Encuentra estudiantes que tienen al menos un curso con un `level` y más de N créditos.
```java
List<Student> findByCoursesLevelAndCoursesCreditsGreaterThan(Level level, int credits);
// repo.findByCoursesLevelAndCoursesCreditsGreaterThan(Level.ADVANCED, 3);
```

---

### 38. `findByActiveTrue` con paginación
Devuelve estudiantes activos paginados. `Pageable` controla página y tamaño.
```java
Page<Student> findByActiveTrue(Pageable pageable);

// Uso:
Page<Student> page = repo.findByActiveTrue(
    PageRequest.of(0, 20, Sort.by("gpa").descending())
);
// Página 0, 20 resultados, ordenados por gpa descendente
```

---

### 39. `findByCoursesStartDateBefore`
Encuentra estudiantes que tienen cursos cuyo `startDate` es ANTERIOR a una fecha.
```java
List<Student> findByCoursesStartDateBefore(Timestamp date);
// repo.findByCoursesStartDateBefore(Timestamp.valueOf(LocalDateTime.now()));
```

---

### 40. `findByFirstNameIgnoreCaseAndLastNameIgnoreCase`
Busca comparando `firstName` Y `lastName` ignorando mayúsculas/minúsculas en ambos.
```java
List<Student> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String first, String last);
```

---

### 41. `findDistinctByFirstNameAndLastName`
Devuelve resultados distintos al filtrar por nombre y apellido.
```java
List<Student> findDistinctByFirstNameAndLastName(String first, String last);
```

---

### 42. `findByEmailEndingWith`
Busca estudiantes cuyo email TERMINA con una cadena dada.
```java
List<Student> findByEmailEndingWith(String suffix);
// repo.findByEmailEndingWith("@gmail.com");
```

---

### 43. `findByFirstNameOrderByLastNameAsc`
Devuelve estudiantes con un `firstName` dado, ordenados por `lastName` ascendente.
```java
List<Student> findByFirstNameOrderByLastNameAsc(String firstName);
```

---

### 44. `findByLastNameOrderByFirstNameDesc`
Devuelve estudiantes con un `lastName` dado, ordenados por `firstName` descendente.
```java
List<Student> findByLastNameOrderByFirstNameDesc(String lastName);
```

---

### 45. `findByCoursesCodeOrderByCoursesCreditsDesc`
Busca estudiantes por código de curso y ordena por créditos del curso descendente.
```java
List<Student> findByCoursesCodeOrderByCoursesCreditsDesc(String courseCode);
// repo.findByCoursesCodeOrderByCoursesCreditsDesc("CS101");
```

---

### 46. `streamByActiveTrue`
Devuelve un `Stream` de estudiantes activos (útil para procesar grandes volúmenes sin cargar todo en memoria).
```java
Stream<Student> streamByActiveTrue();

// Uso — siempre cerrar el stream:
try (Stream<Student> s = repo.streamByActiveTrue()) {
    s.forEach(student -> System.out.println(student.getUsername()));
}
```

---

### 47. `findTopByOrderByGpaAsc`
Devuelve el estudiante con MENOR GPA (el primero ordenando de menor a mayor).
```java
Optional<Student> findTopByOrderByGpaAsc();
```

---

### 48. `findByRegistrationDateBetween` (por año)
Busca estudiantes registrados en un año determinado usando Between en fechas.
```java
List<Student> findByRegistrationDateBetween(Timestamp startOfYear, Timestamp endOfYear);

// Uso para el año 2024:
repo.findByRegistrationDateBetween(
    Timestamp.valueOf("2024-01-01 00:00:00"),
    Timestamp.valueOf("2024-12-31 23:59:59")
);
```

---

### 49. `findByFirstNameNot`
Encuentra estudiantes cuyo `firstName` NO sea el dado.
```java
List<Student> findByFirstNameNot(String name);
// repo.findByFirstNameNot("Admin") → todos excepto los que se llaman "Admin"
```

---

### 50. `findByEmailContainingIgnoreCaseAndActiveTrue`
Combinación: email contiene X (ignorando mayúsculas) Y está activo.
```java
List<Student> findByEmailContainingIgnoreCaseAndActiveTrue(String fragment);
// repo.findByEmailContainingIgnoreCaseAndActiveTrue("example");
```

---

## Ejercicios bonus (del modelo)

### `findDistinctByCoursesDepartmentName`
Devuelve estudiantes distintos que estén en cursos de un departamento con nombre dado.
```java
List<Student> findDistinctByCoursesDepartmentName(String deptName);
// repo.findDistinctByCoursesDepartmentName("Mathematics");
```

### `findByEnrollmentsCourseCodeAndEnrollmentsStatus`
Encuentra estudiantes por código de curso Y estado de la inscripción.
```java
List<Student> findByEnrollmentsCourseCodeAndEnrollmentsStatus(
    String courseCode, EnrollmentStatus status
);
// repo.findByEnrollmentsCourseCodeAndEnrollmentsStatus("CS101", EnrollmentStatus.ENROLLED);
```

### `existsByUsername`
Comprueba existencia por `username`.
```java
boolean existsByUsername(String username);
// boolean e = repo.existsByUsername("kevin");
```

### `deleteAllByActiveFalse`
Borra TODOS los estudiantes inactivos.
```java
void deleteAllByActiveFalse();
```
> ⚠️ Requiere `@Transactional`.

### `findByFirstNameOrLastName`
Busca estudiantes cuyo `firstName` O `lastName` coincida (no ambos, basta uno).
```java
List<Student> findByFirstNameOrLastName(String first, String last);
// repo.findByFirstNameOrLastName("Carlos", "Gomez");
// Devuelve estudiantes llamados "Carlos" + estudiantes con apellido "Gomez"
```

### `findByFirstNameNotContaining`
Encuentra estudiantes cuyo `firstName` NO contiene una subcadena.
```java
List<Student> findByFirstNameNotContaining(String fragment);
// repo.findByFirstNameNotContaining("test");
```

---

## Resumen de keywords

| Keyword | Ejemplo | SQL equivalente |
|---|---|---|
| `And` | `findByNombreAndEdad` | `WHERE nombre=? AND edad=?` |
| `Or` | `findByNombreOrEmail` | `WHERE nombre=? OR email=?` |
| `Not` | `findByNombreNot` | `WHERE nombre <> ?` |
| `Between` | `findByEdadBetween` | `WHERE edad BETWEEN ? AND ?` |
| `LessThan` | `findByEdadLessThan` | `WHERE edad < ?` |
| `LessThanEqual` | `findByEdadLessThanEqual` | `WHERE edad <= ?` |
| `GreaterThan` | `findByEdadGreaterThan` | `WHERE edad > ?` |
| `GreaterThanEqual` | `findByEdadGreaterThanEqual` | `WHERE edad >= ?` |
| `After` | `findByFechaAfter` | `WHERE fecha > ?` |
| `Before` | `findByFechaBefore` | `WHERE fecha < ?` |
| `IsNull` | `findByGpaIsNull` | `WHERE gpa IS NULL` |
| `IsNotNull` | `findByGpaIsNotNull` | `WHERE gpa IS NOT NULL` |
| `Like` | `findByEmailLike` | `WHERE email LIKE ?` |
| `NotLike` | `findByEmailNotLike` | `WHERE email NOT LIKE ?` |
| `StartingWith` | `findByNombreStartingWith` | `WHERE nombre LIKE ?%` |
| `EndingWith` | `findByNombreEndingWith` | `WHERE nombre LIKE %?` |
| `Containing` | `findByNombreContaining` | `WHERE nombre LIKE %?%` |
| `NotContaining` | `findByNombreNotContaining` | `WHERE nombre NOT LIKE %?%` |
| `IgnoreCase` | `findByNombreIgnoreCase` | `WHERE UPPER(nombre)=UPPER(?)` |
| `In` | `findByNombreIn` | `WHERE nombre IN (...)` |
| `NotIn` | `findByNombreNotIn` | `WHERE nombre NOT IN (...)` |
| `True` | `findByActiveTrue` | `WHERE active = true` |
| `False` | `findByActiveFalse` | `WHERE active = false` |
| `OrderBy...Asc` | `findByNombreOrderByEdadAsc` | `ORDER BY edad ASC` |
| `OrderBy...Desc` | `findByNombreOrderByEdadDesc` | `ORDER BY edad DESC` |
| `Top` / `First` | `findTop5By...` | `LIMIT 5` |
| `Distinct` | `findDistinctByNombre` | `SELECT DISTINCT ...` |
| `exists...` | `existsByEmail` | `SELECT COUNT(*) > 0 ...` |
| `count...` | `countByActiveTrue` | `SELECT COUNT(*) ...` |
| `delete...` | `deleteByUsername` | `DELETE FROM ... WHERE ...` |

---

## Cuándo NO usar query methods

Si el nombre del método queda muy largo o la lógica es compleja, usa `@Query`:

```java
@Query("SELECT s FROM Student s WHERE s.gpa > :minGpa AND s.active = true ORDER BY s.gpa DESC")
List<Student> findActiveStudentsWithHighGpa(@Param("minGpa") Double minGpa);
```