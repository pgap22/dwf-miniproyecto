````markdown
# Guía paso a paso: crear un recurso desde 0 (tomando **Users** como base)

> Esta guía te lleva **desde cero** hasta tener un recurso REST completo en Spring Boot 3 (entidad, migración, repositorio, DTOs, mapper, servicio, controlador y **tests**). Usaremos el recurso **Users** del proyecto como ejemplo y patrón para que luego puedas replicarlo con cualquier otro recurso.

---

## 🧭 Índice

1. [Prerequisitos](#prerequisitos)
2. [Arranque rápido](#arranque-rápido)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Paso 1: Migración Flyway (DB)](#paso-1-migración-flyway-db)
5. [Paso 2: Entidad en `domain`](#paso-2-entidad-en-domain)
6. [Paso 3: Repositorio JPA](#paso-3-repositorio-jpa)
7. [Paso 4: DTOs (Request/Response) + Validación](#paso-4-dtos-requestresponse--validación)
8. [Paso 5: Mapper (MapStruct)](#paso-5-mapper-mapstruct)
9. [Paso 6: Servicio + Implementación](#paso-6-servicio--implementación)
10. [Paso 7: Controlador REST](#paso-7-controlador-rest)
11. [Paso 8: Tests (unitarios y de slice)](#paso-8-tests-unitarios-y-de-slice)
12. [Probar los endpoints (curl)](#probar-los-endpoints-curl)
13. [Checklist para crear **otro** recurso](#checklist-para-crear-otro-recurso)
14. [Consejos y problemas comunes](#consejos-y-problemas-comunes)

---

## Prerequisitos

- **Java 17+**
- **Maven** (el repo trae `./mvnw`/`mvnw.cmd`)
- **MySQL** para `dev` (H2 se usa en `test`)
- IDE con soporte de **annotation processing** (MapStruct + Lombok)

---

## Arranque rápido

1. Copia `.env.example` a `.env` y ajusta variables:
   ```properties
   SPRING_PROFILES_ACTIVE=dev
   DB_URL=jdbc:mysql://localhost:3306/data_collector_dev?createDatabaseIfNotExist=true&serverTimezone=UTC
   DB_user=root
   DB_PASS=tu_password
````

> El proyecto usa **spring-dotenv**: Spring leerá `.env` y activará el perfil indicado.

2. Levanta el proyecto:

   ```bash
   ./mvnw spring-boot:run
   # o
   mvn spring-boot:run
   ```

3. Ejecuta los tests:

   ```bash
   ./mvnw clean verify
   ```

---

## Estructura del proyecto

```
src/
  main/
    java/sv/edu/udb/data_collector/
      controller/
        request/         # DTOs de entrada
        response/        # DTOs de salida
      domain/            # Entidades JPA
      repository/        # Repositorios Spring Data
      security/hasher/   # Utilidades (e.g., BCrypt)
      service/
        implementation/  # Implementaciones de servicios
        mapper/          # MapStruct mappers
    resources/
      application.yml
      application-dev.yml
      application-test.yml
      db/migration/      # Migraciones Flyway (V1__, V2__, ...)
```

> En `pom.xml` ya está configurado **MapStruct**, **Lombok** y el **annotation processor** necesario.

---

## Paso 1: Migración Flyway (DB)

Crea la tabla con una migración en `src/main/resources/db/migration`:

**Ejemplo (Users):** `V1__create_users.sql`

```sql
CREATE TABLE IF NOT EXISTS users (
  id             VARCHAR(36)  NOT NULL,
  name           VARCHAR(150) NOT NULL,
  email          VARCHAR(191) NOT NULL,
  password_hash  VARCHAR(60)  NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uq_users_email UNIQUE (email)
);
```

> **Perfiles**:
>
> * `application-dev.yml` apunta a **MySQL**.
> * `application-test.yml` usa **H2** en memoria con Flyway habilitado.
> * `application.yml` define `ddl-auto: validate` (Hibernate **valida** contra migraciones).

---

## Paso 2: Entidad en `domain`

Crea la entidad JPA con **UUID** como id (ejemplo Users):

`src/main/java/.../domain/User.java`

```java
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
```

> **Notas**
>
> * `@UuidGenerator` delega la generación del id a Hibernate.
> * Usa longitudes acordes a la migración Flyway.

---

## Paso 3: Repositorio JPA

`src/main/java/.../repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

> Regla de oro: expón **sólo** queries realmente necesarias.

---

## Paso 4: DTOs (Request/Response) + Validación

**Request** (entrada):
`controller/request/UserRequest.java`

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    @Email(message = "Debe ser un email válido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
```

**Response** (salida):
`controller/response/UserResponse.java`

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String name;
    private String email;
}
```

---

## Paso 5: Mapper (MapStruct)

Convierte entre DTOs y Entity. Configurado como **Spring bean**:

`service/mapper/UserMapper.java`

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", source = "password") // el hash real se hace en el servicio
  User toUser(UserRequest req);

  UserResponse toUserResponse(User user);
}
```

> **Compilación**: El `maven-compiler-plugin` ya declara los `annotationProcessorPaths` (MapStruct + Lombok). Asegúrate de que tu IDE use *annotation processing*.

---

## Paso 6: Servicio + Implementación

**Interface**
`service/UserService.java`

```java
public interface UserService {
  UserResponse create(UserRequest req);
  UserResponse findById(String id);
  List<UserResponse> list();
}
```

**Hasher** (ya existe):
`security/hasher/PasswordHasher.java` y `BCryptPasswordHasher.java`

> Se usa **BCrypt** para contraseñas.

**Implementación** (idea general):
`service/implementation/UserServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repo;
  private final UserMapper userMapper;
  private final PasswordHasher passwordHasher;

  @Override
  public UserResponse create(UserRequest req) {
    // 1) mapear DTO -> Entity
    User entity = userMapper.toUser(req);

    // 2) hashear la contraseña
    entity.setPasswordHash(passwordHasher.hash(req.getPassword()));

    // 3) persistir
    User saved = repo.save(entity);

    // 4) mapear Entity -> Response
    return userMapper.toUserResponse(saved);
  }

  @Override
  public UserResponse findById(String id) {
    return repo.findById(id)
      .map(userMapper::toUserResponse)
      .orElseThrow(() -> new EntityNotFoundException("User no encontrado"));
  }

  @Override
  public List<UserResponse> list() {
    return repo.findAll().stream()
      .map(userMapper::toUserResponse)
      .toList();
  }
}
```

---

## Paso 7: Controlador REST

`controller/UserController.java`

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping
  public UserResponse create(@Valid @RequestBody UserRequest req) {
    return userService.create(req);
  }

  @GetMapping("/{id}")
  public UserResponse findById(@PathVariable String id) {
    return userService.findById(id);
  }

  @GetMapping
  public List<UserResponse> list() {
    return userService.list();
  }
}
```

> En `DataCollectorApplication` se excluye `SecurityAutoConfiguration` para simplificar:
>
> ```java
> @SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
> ```

---

## Paso 8: Tests (unitarios y de slice)

> El proyecto ya incluye **tests de ejemplo** para Users. Úsalos como plantilla al crear otros recursos.

### 8.1. Hasher (unit test puro)

`security/hasher/BCryptPasswordHasherTest.java`

```java
class BCryptPasswordHasherTest {
  @Test
  void hash_and_matches() {
    BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    String raw = "S3cret!";
    String hash = hasher.hash(raw);

    assertThat(hash).isNotBlank();
    assertThat(hasher.matches(raw, hash)).isTrue();
    assertThat(hasher.matches("wrong", hash)).isFalse();
  }
}
```

### 8.2. Mapper (unit test de MapStruct)

`service/mapper/UserMapperTest.java`

```java
class UserMapperTest {
  private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

  @Test
  void toUser_maps_basic_fields_and_password_to_hash_field() {
    UserRequest req = UserRequest.builder()
        .name("Bart").email("bart@pukis.com").password("123").build();

    User u = mapper.toUser(req);

    assertThat(u.getName()).isEqualTo("Bart");
    assertThat(u.getEmail()).isEqualTo("bart@pukis.com");
    // aquí va el raw password (luego el servicio lo hashea)
    assertThat(u.getPasswordHash()).isEqualTo("123");
  }

  @Test
  void toUserResponse_maps_fields() {
    User u = new User();
    u.setId("1"); u.setName("Bart"); u.setEmail("bart@pukis.com");
    UserResponse resp = mapper.toUserResponse(u);
    assertThat(resp.getId()).isEqualTo("1");
    assertThat(resp.getName()).isEqualTo("Bart");
    assertThat(resp.getEmail()).isEqualTo("bart@pukis.com");
  }
}
```

### 8.3. Servicio (Mockito)

`service/UserServiceImplTest.java`

```java
class UserServiceImplTest {
  private UserRepository repo;
  private PasswordHasher hasher;
  private UserMapper mapper;
  private UserServiceImpl service;

  @BeforeEach
  void setup() {
    repo = mock(UserRepository.class);
    hasher = mock(PasswordHasher.class);
    mapper = mock(UserMapper.class);
    service = new UserServiceImpl(repo, mapper, hasher);
  }

  @Test
  void create_hashes_password_and_returns_response() {
    UserRequest req = UserRequest.builder()
        .name("Bart").email("bart@pukis.com").password("123").build();

    User entity = new User();
    when(mapper.toUser(req)).thenReturn(entity);
    when(hasher.hash("123")).thenReturn("HASHED");
    when(repo.save(entity)).thenAnswer(inv -> {
      entity.setId("uuid-1");
      entity.setPasswordHash("HASHED");
      return entity;
    });
    when(mapper.toUserResponse(entity))
      .thenReturn(new UserResponse("uuid-1","Bart","bart@pukis.com"));

    UserResponse resp = service.create(req);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(repo).save(captor.capture());
    assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASHED");
    assertThat(resp.getId()).isEqualTo("uuid-1");
  }

  @Test
  void findById_when_not_found_throws() {
    when(repo.findById("x")).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.findById("x"));
  }

  @Test
  void list_maps_all() {
    User u = new User();
    u.setId("1"); u.setName("A"); u.setEmail("a@a.com"); u.setPasswordHash("x");
    when(repo.findAll()).thenReturn(List.of(u));
    when(mapper.toUserResponse(u)).thenReturn(new UserResponse("1","A","a@a.com"));

    var list = service.list();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getEmail()).isEqualTo("a@a.com");
  }
}
```

### 8.4. Repositorio (DataJpaTest + H2 + Flyway)

`repository/UserRepositoryTest.java`

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {
  @Autowired UserRepository userRepository;

  @Test
  void findByEmail_and_exists() {
    User u = User.builder()
        .name("Bart")
        .email("bart@pukis.com")
        // dummy bcrypt (len=60)
        .passwordHash("$2a$10$abcdefghijABCDEFGHIJabcdefghijABCDEFGHIJabcdefghijAB")
        .build();

    u = userRepository.save(u);
    assertThat(u.getId()).isNotBlank();

    Optional<User> byEmail = userRepository.findByEmail("bart@pukis.com");
    assertThat(byEmail).isPresent();
    assertThat(userRepository.existsByEmail("bart@pukis.com")).isTrue();
    assertThat(userRepository.existsByEmail("no@no.com")).isFalse();
  }
}
```

> **Por qué funciona con H2**: `application-test.yml` configura H2 y habilita Flyway para recrear el esquema a partir de tus migraciones.

### 8.5. Controlador (WebMvcTest + MockMvc)

`controller/UserControllerTest.java`

```java
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de Security
class UserControllerTest {
  @Autowired private MockMvc mvc;
  @MockBean private UserService userService;

  @Test
  void create_returns_200_and_body() throws Exception {
    when(userService.create(any(UserRequest.class)))
      .thenReturn(new UserResponse("1","Bart","bart@pukis.com"));

    String json = """
      {"name":"Bart","email":"bart@pukis.com","password":"123"}
    """;

    mvc.perform(post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value("1"))
      .andExpect(jsonPath("$.email").value("bart@pukis.com"));
  }

  @Test
  void get_by_id_ok() throws Exception {
    when(userService.findById("1"))
      .thenReturn(new UserResponse("1","Bart","bart@pukis.com"));

    mvc.perform(get("/users/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Bart"));
  }

  @Test
  void list_ok() throws Exception {
    when(userService.list()).thenReturn(List.of(
      new UserResponse("1","A","a@a.com"),
      new UserResponse("2","B","b@b.com")
    ));

    mvc.perform(get("/users"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].email").value("a@a.com"))
      .andExpect(jsonPath("$[1].email").value("b@b.com"));
  }
}
```

---

## Probar los endpoints (curl)

```bash
# Crear usuario
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Lisa","email":"lisa@pukis.com","password":"123"}'

# Obtener por id
curl http://localhost:8080/users/<UUID>

# Listar
curl http://localhost:8080/users
```

---

## Checklist para crear **otro** recurso

> Supón que quieres crear `Project` replicando el patrón de `User`.

1. **DB (Flyway)**: `V2__create_projects.sql`
2. **Entidad**: `domain/Project.java`
3. **Repositorio**: `repository/ProjectRepository.java`
4. **DTOs**: `controller/request/ProjectRequest.java`, `controller/response/ProjectResponse.java`
5. **Mapper**: `service/mapper/ProjectMapper.java`
6. **Servicio**: `service/ProjectService.java`
7. **Implementación**: `service/implementation/ProjectServiceImpl.java`
8. **Controlador**: `controller/ProjectController.java` (`@RequestMapping("/projects")`)
9. **Tests**:

   * `mapper/ProjectMapperTest.java`
   * `service/ProjectServiceImplTest.java`
   * `repository/ProjectRepositoryTest.java`
   * `controller/ProjectControllerTest.java`
10. **Arrancar tests**: `./mvnw clean verify`

### Plantillas mínimas (renombra `User` → `Project`)

* **Migración**

  ```sql
  CREATE TABLE IF NOT EXISTS projects (
    id   VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    CONSTRAINT pk_projects PRIMARY KEY (id)
  );
  ```

* **Entidad**

  ```java
  @Entity @Table(name="projects")
  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public class Project {
    @Id @UuidGenerator @Column(nullable=false, updatable=false, length=36)
    private String id;

    @Column(nullable=false, length=150)
    private String name;
  }
  ```

* **Repositorio**

  ```java
  public interface ProjectRepository extends JpaRepository<Project, String> {}
  ```

* **DTOs**

  ```java
  public class ProjectRequest { @NotBlank String name; }
  public class ProjectResponse { String id; String name; }
  ```

* **Mapper**

  ```java
  @Mapper(componentModel = "spring")
  public interface ProjectMapper {
    @Mapping(target="id", ignore=true)
    Project toProject(ProjectRequest req);
    ProjectResponse toProjectResponse(Project p);
  }
  ```

* **Servicio**

  ```java
  public interface ProjectService {
    ProjectResponse create(ProjectRequest req);
    ProjectResponse findById(String id);
    List<ProjectResponse> list();
  }
  ```

* **Impl**

  ```java
  @Service @RequiredArgsConstructor
  public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository repo;
    private final ProjectMapper mapper;

    public ProjectResponse create(ProjectRequest req) {
      Project entity = mapper.toProject(req);
      return mapper.toProjectResponse(repo.save(entity));
    }
    public ProjectResponse findById(String id) {
      return repo.findById(id).map(mapper::toProjectResponse)
        .orElseThrow(() -> new EntityNotFoundException("Project no encontrado"));
    }
    public List<ProjectResponse> list() {
      return repo.findAll().stream().map(mapper::toProjectResponse).toList();
    }
  }
  ```

* **Controlador**

  ```java
  @RestController @RequestMapping("/projects") @RequiredArgsConstructor
  public class ProjectController {
    private final ProjectService service;

    @PostMapping public ProjectResponse create(@Valid @RequestBody ProjectRequest req) { return service.create(req); }
    @GetMapping("/{id}") public ProjectResponse findById(@PathVariable String id) { return service.findById(id); }
    @GetMapping public List<ProjectResponse> list() { return service.list(); }
  }
  ```

* **Tests**: copia los de `User*Test` y ajusta nombres/tipos/rutas.

---

## Consejos y problemas comunes

* **Annotation processing**: si el mapper no se genera (`UserMapperImpl`), revisa:

  * `pom.xml` → `maven-compiler-plugin` con `annotationProcessorPaths`.
  * IDE: habilitar *annotation processing*.
* **Flyway + H2**: en `test` se usa `H2` con modo `MySQL` y Flyway habilitado; no mezcles `ddl-auto: update`. Mantén `validate`.
* **Contraseñas**: Nunca expongas `passwordHash` en responses. Hashea en el servicio (no en el mapper).
* **Validación**: usa `@Valid` en controladores y `jakarta.validation` en DTOs.
* **Errores 400/404**: `@Valid` genera 400; `EntityNotFoundException` mapea a 404 si configuras un `@ControllerAdvice` (opcional).
* **Profiles**: `.env` define `SPRING_PROFILES_ACTIVE` (`dev`, `test`, `prod`). Ajusta `application-<profile>.yml`.
* **Límites de longitud**: mantén consistencia entre columnas SQL y `@Column(length=...)`.

---

¡Listo! Con este patrón (basado en **Users**) puedes crear cualquier recurso nuevo de forma consistente, con pruebas y migraciones desde el primer día.

```
```
