package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import sv.edu.udb.data_collector.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Constantes comunes
    private static final String GIVEN_NAME = "Bart";
    private static final String GIVEN_PASSWORD_HASH =
            "$2a$10$abcdefghijABCDEFGHIJabcdefghijABCDEFGHIJabcdefghijAB"; // bcrypt dummy

    private static final String EMAIL_EXISTING = "bart@pukis.com";
    private static final String EMAIL_OTHER    = "lisa@pukis.com";
    private static final String EMAIL_MISSING  = "no@no.com";

    // Utilidad para crear un usuario
    private User createUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .passwordHash(GIVEN_PASSWORD_HASH)
                .build();
    }

    @Test
    @DisplayName("save: genera ID y persiste campos")
    void save_persistsUser_andGeneratesId() {
        // Arrange
        User userBuilt = createUser(GIVEN_NAME, EMAIL_EXISTING);

        // Act
        User savedUser = userRepository.save(userBuilt);   // breakpoint aquí

        // Assert
        assertThat(savedUser.getId()).isNotBlank();
        assertThat(savedUser.getName()).isEqualTo(GIVEN_NAME);
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL_EXISTING);
    }

    @Test
    @DisplayName("findByEmail: devuelve usuario existente")
    void findByEmail_returnsUser_whenExists() {
        // Arrange
        User userBuilt = createUser(GIVEN_NAME, EMAIL_EXISTING);
        userRepository.save(userBuilt);   // breakpoint aquí

        // Act
        Optional<User> foundByEmail = userRepository.findByEmail(EMAIL_EXISTING); // breakpoint aquí

        // Assert
        assertThat(foundByEmail).isPresent();
        foundByEmail.ifPresent(found -> {
            String foundId = found.getId();
            String foundName = found.getName();
            String foundEmail = found.getEmail();

            assertThat(foundId).isNotBlank();
            assertThat(foundName).isEqualTo(GIVEN_NAME);
            assertThat(foundEmail).isEqualTo(EMAIL_EXISTING);
        });
    }

    @Test
    @DisplayName("findByEmail: devuelve vacío cuando no existe")
    void findByEmail_returnsEmpty_whenNotExists() {
        // Arrange
        User userBuilt = createUser(GIVEN_NAME, EMAIL_OTHER);
        userRepository.save(userBuilt);   // breakpoint aquí

        // Act
        Optional<User> foundByEmail = userRepository.findByEmail(EMAIL_MISSING); // breakpoint aquí

        // Assert
        assertThat(foundByEmail).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail: true cuando existe")
    void existsByEmail_true_whenExists() {
        // Arrange
        User userBuilt = createUser(GIVEN_NAME, EMAIL_EXISTING);
        userRepository.save(userBuilt);   // breakpoint aquí

        // Act
        boolean exists = userRepository.existsByEmail(EMAIL_EXISTING); // breakpoint aquí

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: false cuando no existe")
    void existsByEmail_false_whenNotExists() {
        // Arrange
        User userBuilt = createUser(GIVEN_NAME, EMAIL_OTHER);
        userRepository.save(userBuilt);   // breakpoint aquí

        // Act
        boolean exists = userRepository.existsByEmail(EMAIL_MISSING); // breakpoint aquí

        // Assert
        assertThat(exists).isFalse();
    }
}
