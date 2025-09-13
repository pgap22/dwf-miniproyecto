package sv.edu.udb.data_collector.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.udb.data_collector.controller.request.LoginRequest;
import sv.edu.udb.data_collector.controller.response.LoginResponse;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.security.hasher.PasswordHasher;
import sv.edu.udb.data_collector.service.AuthService;
import sv.edu.udb.data_collector.service.mapper.UserMapper;
import sv.edu.udb.data_collector.security.token.TokenService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        var user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Credenciales inválidas"));

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenService.generateAccessToken(user.getId(), user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(userMapper.toUserResponse(user))
                .build();
    }
}
