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
## Paso 8: Pruebas (Unitarias y Slice)

> Todos los tests siguen el patrón **Arrange – Act – Assert**
> para que el flujo sea claro y depurable.

---

### 8.1 Hasher – *Unit Test Puro*

Archivo: `security/hasher/BCryptPasswordHasherTest.java`

```java
class BCryptPasswordHasherTest {

    @Test
    @DisplayName("hash: genera hash y matches valida correctamente")
    void hash_and_matches() {
        // ---------- Arrange ----------
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String rawPassword   = "S3cret!";
        String wrongPassword = "wrong";

        // ---------- Act ----------
        String hashed = hasher.hash(rawPassword);
        boolean matchesCorrect = hasher.matches(rawPassword, hashed);
        boolean matchesWrong   = hasher.matches(wrongPassword, hashed);

        // ---------- Assert ----------
        assertThat(hashed).as("Hash no debe ser nulo ni vacío").isNotBlank();
        assertThat(matchesCorrect).as("Debe coincidir con la contraseña original").isTrue();
        assertThat(matchesWrong).as("No debe coincidir con una contraseña incorrecta").isFalse();
    }
}
```

---

### 8.2 Mapper – *Unit Test con MapStruct*

Archivo: `service/mapper/UserMapperTest.java`

```java
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("toUser: mapea campos básicos y password -> passwordHash")
    void toUser_maps_basic_fields_and_password_to_hash_field() {
        // ---------- Arrange ----------
        String givenName = "Bart";
        String givenEmail = "bart@pukis.com";
        String givenPassword = "123";

        UserRequest request = UserRequest.builder()
                .name(givenName)
                .email(givenEmail)
                .password(givenPassword)
                .build();

        // ---------- Act ----------
        User mappedUser = mapper.toUser(request);

        // ---------- Assert ----------
        assertThat(mappedUser.getId()).as("ID debe ser nulo al mapear").isNull();
        assertThat(mappedUser.getName()).isEqualTo(givenName);
        assertThat(mappedUser.getEmail()).isEqualTo(givenEmail);
        assertThat(mappedUser.getPasswordHash()).isEqualTo(givenPassword);
    }

    @Test
    @DisplayName("toUserResponse: mapea campos de dominio a DTO")
    void toUserResponse_maps_fields() {
        // ---------- Arrange ----------
        String userId = "1";
        String userName = "Bart";
        String userEmail = "bart@pukis.com";
        String userHash = "HASH";

        User domainUser = new User();
        domainUser.setId(userId);
        domainUser.setName(userName);
        domainUser.setEmail(userEmail);
        domainUser.setPasswordHash(userHash);

        // ---------- Act ----------
        UserResponse response = mapper.toUserResponse(domainUser);

        // ---------- Assert ----------
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getName()).isEqualTo(userName);
        assertThat(response.getEmail()).isEqualTo(userEmail);
    }
}
```

---

### 8.3 Servicio – *Unit Test con Mockito*

Archivo: `service/UserServiceImplTest.java`

```java
class UserServiceImplTest {

    // ---------- Constantes GIVEN / EXPECTED ----------
    private static final String GIVEN_NAME = "Bart";
    private static final String GIVEN_EMAIL = "bart@pukis.com";
    private static final String GIVEN_PASSWORD = "123";
    private static final String EXPECTED_HASH = "HASHED";
    private static final String EXPECTED_ID = "uuid-1";
    private static final String NOT_FOUND_ID = "x";

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
    @DisplayName("create: hashea password, guarda y retorna respuesta mapeada")
    void create_hashes_password_and_returns_response() {
        // ---------- Arrange ----------
        UserRequest request = UserRequest.builder()
                .name(GIVEN_NAME)
                .email(GIVEN_EMAIL)
                .password(GIVEN_PASSWORD)
                .build();

        User mappedEntity = new User();
        when(mapper.toUser(request)).thenReturn(mappedEntity);
        when(hasher.hash(GIVEN_PASSWORD)).thenReturn(EXPECTED_HASH);
        when(repo.save(mappedEntity)).thenAnswer(inv -> {
            mappedEntity.setId(EXPECTED_ID);
            mappedEntity.setPasswordHash(EXPECTED_HASH);
            mappedEntity.setName(GIVEN_NAME);
            mappedEntity.setEmail(GIVEN_EMAIL);
            return mappedEntity;
        });
        UserResponse expectedResponse = new UserResponse(EXPECTED_ID, GIVEN_NAME, GIVEN_EMAIL);
        when(mapper.toUserResponse(mappedEntity)).thenReturn(expectedResponse);

        // ---------- Act ----------
        UserResponse actualResponse = service.create(request);

        // ---------- Assert ----------
        ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(savedCaptor.capture());
        User saved = savedCaptor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo(EXPECTED_HASH);
        assertThat(actualResponse.getId()).isEqualTo(EXPECTED_ID);
        assertThat(actualResponse.getName()).isEqualTo(GIVEN_NAME);
        assertThat(actualResponse.getEmail()).isEqualTo(GIVEN_EMAIL);
    }

    @Test
    @DisplayName("findById: lanza EntityNotFound cuando no existe")
    void findById_when_not_found_throws() {
        // ---------- Arrange ----------
        when(repo.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        // ---------- Act + Assert ----------
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> service.findById(NOT_FOUND_ID));
        verify(repo).findById(NOT_FOUND_ID);
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("list: devuelve todos mapeados a UserResponse")
    void list_maps_all() {
        // ---------- Arrange ----------
        User stored = new User();
        stored.setId("1");
        stored.setName("A");
        stored.setEmail("a@a.com");
        stored.setPasswordHash("x");

        when(repo.findAll()).thenReturn(List.of(stored));
        UserResponse mapped = new UserResponse("1", "A", "a@a.com");
        when(mapper.toUserResponse(stored)).thenReturn(mapped);

        // ---------- Act ----------
        List<UserResponse> result = service.list();

        // ---------- Assert ----------
        assertThat(result).hasSize(1);
        UserResponse first = result.get(0);
        assertThat(first.getId()).isEqualTo("1");
        assertThat(first.getName()).isEqualTo("A");
        assertThat(first.getEmail()).isEqualTo("a@a.com");

        verify(repo).findAll();
        verify(mapper).toUserResponse(stored);
        verifyNoMoreInteractions(repo, mapper);
    }
}
```

---

### 8.4 Controlador – *Slice Test con WebMvcTest + MockMvc*

Archivo: `controller/UserControllerTest.java`

```java
@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { SecurityConfig.class, JwtAuthenticationFilter.class }
        )
)
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    // ---------- GIVEN / EXPECTED ----------
    private static final String USER_ID_1 = "1";
    private static final String USER_ID_404 = "999";
    private static final String USER_NAME_BART = "Bart";
    private static final String EMAIL_BART = "bart@pukis.com";

    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("GET /users/{id} → 200 y body esperado")
    void get_by_id_ok() throws Exception {
        // ---------- Arrange ----------
        UserResponse expected = new UserResponse(USER_ID_1, USER_NAME_BART, EMAIL_BART);
        when(userService.findById(USER_ID_1)).thenReturn(expected);

        // ---------- Act ----------
        var result = mvc.perform(get("/users/{id}", USER_ID_1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_OK);
        UserResponse actual = MAPPER.readValue(body, UserResponse.class);
        assertThat(actual.getId()).isEqualTo(USER_ID_1);
        assertThat(actual.getName()).isEqualTo(USER_NAME_BART);
        assertThat(actual.getEmail()).isEqualTo(EMAIL_BART);
    }

    @Test
    @DisplayName("GET /users → 200 y lista")
    void list_ok() throws Exception {
        // ---------- Arrange ----------
        List<UserResponse> expected = List.of(
                new UserResponse("1", "A", "a@a.com"),
                new UserResponse("2", "B", "b@b.com")
        );
        when(userService.list()).thenReturn(expected);

        // ---------- Act ----------
        var result = mvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_OK);
        List<UserResponse> actual = MAPPER.readValue(body,
                new TypeReference<List<UserResponse>>() {});
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getEmail()).isEqualTo("a@a.com");
        assertThat(actual.get(1).getEmail()).isEqualTo("b@b.com");
    }

    @Test
    @DisplayName("POST /users → 200 y body creado")
    void create_returns_200_and_body() throws Exception {
        // ---------- Arrange ----------
        UserResponse expected = new UserResponse(USER_ID_1, USER_NAME_BART, EMAIL_BART);
        when(userService.create(any(UserRequest.class))).thenReturn(expected);

        String payload = """
            {"name":"Bart","email":"bart@pukis.com","password":"123"}
        """;

        // ---------- Act ----------
        var result = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_OK);
        UserResponse actual = MAPPER.readValue(body, UserResponse.class);
        assertThat(actual.getId()).isEqualTo(USER_ID_1);
        assertThat(actual.getEmail()).isEqualTo(EMAIL_BART);
    }

    @Test
    @DisplayName("POST /users → 400 cuando la validación falla")
    void create_400_validation() throws Exception {
        // ---------- Arrange ----------
        String invalid = """
            {"name":"","email":"mal","password":""}
        """;

        // ---------- Act ----------
        var result = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(body).contains("ViolationFieldError");
        assertThat(body).contains("email");
    }

    @Test
    @DisplayName("GET /users/{id} → 404 cuando no existe")
    void get_by_id_404() throws Exception {
        // ---------- Arrange ----------
        when(userService.findById(USER_ID_404))
                .thenThrow(new EntityNotFoundException("not found"));

        // ---------- Act ----------
        var result = mvc.perform(get("/users/{id}", USER_ID_404)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_NOT_FOUND);
        assertThat(body).contains("not found");
    }
}
```

---

### Recomendaciones finales

* Usa siempre **`@DisplayName` descriptivos**, que aparezcan tal cual en los reportes de JUnit.
* Mantén la estructura **Arrange – Act – Assert** para claridad y facilidad de depuración.
* Los *slice tests* (`@WebMvcTest`, `@DataJpaTest`) aíslan cada capa y ejecutan rápido.
* Para pruebas de integración completas, combina `@SpringBootTest` con un MySQL efímero o Testcontainers.

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
