package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sv.edu.udb.data_collector.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}