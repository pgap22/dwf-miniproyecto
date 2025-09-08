package sv.edu.udb.data_collector.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.security.hasher.PasswordHasher;
import sv.edu.udb.data_collector.service.UserService;
import sv.edu.udb.data_collector.service.mapper.UserMapper;

@Service
@RequiredArgsConstructor
//Los mapper que tengan el nombre de su entidad
public class UserServiceImpl implements UserService {

  private final UserRepository repo;
  private final UserMapper userMapper;
  private final PasswordHasher passwordHasher; // interfaz, no BCrypt directo


  @Override
  public UserResponse create(UserRequest req) {
    if (repo.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("Email ya registrado");
    }
    User user = userMapper.toUser(req);
    user.setPasswordHash(hash(req.getPassword())); 
    return userMapper.toUserResponse(repo.save(user));
  }

  @Override
  public UserResponse findById(String id) {
    return repo.findById(id).map(userMapper::toUserResponse)
      .orElseThrow(() -> new EntityNotFoundException("User no encontrado"));
  }

  @Override
  public List<UserResponse> list() {
    return repo.findAll().stream().map(userMapper::toUserResponse).toList();
  }

  private String hash(String raw) {
    return passwordHasher.hash(raw);
  }
}