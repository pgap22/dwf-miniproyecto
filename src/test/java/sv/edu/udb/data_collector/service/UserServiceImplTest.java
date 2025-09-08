package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
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
                .name("Bart")
                .email("bart@pukis.com")
                .password("123")
                .build();

        User entity = new User();
        when(mapper.toUser(req)).thenReturn(entity);
        when(hasher.hash("123")).thenReturn("HASHED");
        when(repo.save(entity)).thenAnswer(inv -> {
            entity.setId("uuid-1");
            entity.setPasswordHash("HASHED");
            return entity;
        });
        when(mapper.toUserResponse(entity)).thenReturn(new UserResponse("uuid-1","Bart","bart@pukis.com"));

        UserResponse resp = service.create(req);

        // capturar lo que se intentó guardar
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASHED");
        assertThat(resp.getId()).isEqualTo("uuid-1");
    }

    @Test
    void findById_when_not_found_throws() {
        when(repo.findById("x")).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> service.findById("x"));
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
