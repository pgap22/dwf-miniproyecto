package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import sv.edu.udb.data_collector.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_and_findByEmail_and_existsByEmail() {
        User u = User.builder()
                .name("Bart")
                .email("bart@pukis.com")
                .passwordHash("$2a$10$abcdefghijABCDEFGHIJabcdefghijABCDEFGHIJabcdefghijAB") // dummy bcrypt (len=60)
                .build();

        u = userRepository.save(u);

        assertThat(u.getId()).isNotBlank();

        Optional<User> byEmail = userRepository.findByEmail("bart@pukis.com");
        assertThat(byEmail).isPresent();
        assertThat(userRepository.existsByEmail("bart@pukis.com")).isTrue();
        assertThat(userRepository.existsByEmail("no@no.com")).isFalse();
    }
}
