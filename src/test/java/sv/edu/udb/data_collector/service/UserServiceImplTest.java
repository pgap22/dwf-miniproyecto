package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.security.hasher.PasswordHasher;
import sv.edu.udb.data_collector.service.implementation.UserServiceImpl;
import sv.edu.udb.data_collector.service.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    // ---------- Constantes GIVEN / EXPECTED ----------
    private static final String GIVEN_NAME = "Bart";
    private static final String GIVEN_EMAIL = "bart@pukis.com";
    private static final String GIVEN_PASSWORD = "123";
    private static final String EXPECTED_HASH = "HASHED";
    private static final String EXPECTED_ID = "uuid-1";

    private static final String NOT_FOUND_ID = "x";

    // ---------- Mocks & SUT ----------
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

        User mappedEntity = new User(); // resultado de mapper.toUser(request)
        when(mapper.toUser(request)).thenReturn(mappedEntity);

        when(hasher.hash(GIVEN_PASSWORD)).thenReturn(EXPECTED_HASH);

        when(repo.save(mappedEntity)).thenAnswer(inv -> {
            // simulamos persistencia
            mappedEntity.setId(EXPECTED_ID);
            mappedEntity.setPasswordHash(EXPECTED_HASH);
            mappedEntity.setName(GIVEN_NAME);
            mappedEntity.setEmail(GIVEN_EMAIL);
            return mappedEntity;
        });

        UserResponse expectedResponse = new UserResponse(EXPECTED_ID, GIVEN_NAME, GIVEN_EMAIL);
        when(mapper.toUserResponse(mappedEntity)).thenReturn(expectedResponse);

        // ---------- Act ----------
        UserResponse actualResponse = service.create(request); // <- breakpoint útil

        // ---------- Assert ----------
        // Captura del argumento con el que se intentó guardar
        ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(savedCaptor.capture());
        User saved = savedCaptor.getValue();                 // <- breakpoint útil

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
        List<UserResponse> result = service.list();          // <- breakpoint útil

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
