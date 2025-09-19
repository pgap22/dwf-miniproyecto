package sv.edu.udb.data_collector.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.data_collector.controller.request.LoginRequest;
import sv.edu.udb.data_collector.controller.response.LoginResponse;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.security.hasher.PasswordHasher;
import sv.edu.udb.data_collector.security.token.TokenService;
import sv.edu.udb.data_collector.service.implementation.AuthServiceImpl;
import sv.edu.udb.data_collector.service.mapper.UserMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository repo;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Debe devolver un LoginResponse cuando las credenciales son válidas")
    void login_whenCredentialsAreValid_shouldReturnLoginResponse() {
        // Arrange (Organizar)
        var loginRequest = new LoginRequest("test@example.com", "password123");
        var userEntity = User.builder().id("user-1").email("test@example.com").passwordHash("hashed_password").build();
        
        // Simulamos el comportamiento de las dependencias para el caso de éxito
        given(repo.findByEmail("test@example.com")).willReturn(Optional.of(userEntity));
        given(passwordHasher.matches("password123", "hashed_password")).willReturn(true);
        given(tokenService.generateAccessToken(anyString(), anyString())).willReturn("dummy.jwt.token");
        given(userMapper.toUserResponse(any(User.class))).willReturn(new UserResponse());

        // Act (Actuar)
        LoginResponse result = authService.login(loginRequest);

        // Assert (Afirmar)
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("dummy.jwt.token");
        verify(repo).findByEmail("test@example.com");
        verify(passwordHasher).matches("password123", "hashed_password");
        verify(tokenService).generateAccessToken("user-1", "test@example.com");
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el usuario no existe")
    void login_whenUserNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        var loginRequest = new LoginRequest("notfound@example.com", "password123");
        given(repo.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            authService.login(loginRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("Credenciales inválidas");
        verify(passwordHasher, never()).matches(anyString(), anyString()); // Verificamos que el hash nunca se comprueba
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la contraseña es incorrecta")
    void login_whenPasswordIsIncorrect_shouldThrowIllegalArgumentException() {
        // Arrange
        var loginRequest = new LoginRequest("test@example.com", "wrong_password");
        var userEntity = User.builder().id("user-1").email("test@example.com").passwordHash("hashed_password").build();
        
        given(repo.findByEmail("test@example.com")).willReturn(Optional.of(userEntity));
        given(passwordHasher.matches("wrong_password", "hashed_password")).willReturn(false); // La contraseña no coincide

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertThat(exception.getMessage()).isEqualTo("Credenciales inválidas");
        verify(tokenService, never()).generateAccessToken(anyString(), anyString()); // Verificamos que el token nunca se genera
    }
}