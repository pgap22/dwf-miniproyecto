package sv.edu.udb.data_collector.security.hasher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    @Test
    @DisplayName("hash: genera hash y matches valida correctamente")
    void hash_and_matches() {
        // ---------- Arrange ----------
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String rawPassword   = "S3cret!";
        String wrongPassword = "wrong";

        // ---------- Act ----------
        String hashed = hasher.hash(rawPassword);             // <- breakpoint útil
        boolean matchesCorrect = hasher.matches(rawPassword, hashed); // <- breakpoint útil
        boolean matchesWrong   = hasher.matches(wrongPassword, hashed);

        // ---------- Assert ----------
        assertThat(hashed).as("Hash no debe ser nulo ni vacío").isNotBlank();
        assertThat(matchesCorrect).as("Debe coincidir con la contraseña original").isTrue();
        assertThat(matchesWrong).as("No debe coincidir con una contraseña incorrecta").isFalse();
    }
}
