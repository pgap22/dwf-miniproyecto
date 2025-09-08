package sv.edu.udb.data_collector.security.hasher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    @Test
    void hash_and_matches() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String raw = "S3cret!";
        String hash = hasher.hash(raw);

        assertThat(hash).isNotBlank();
        assertThat(hasher.matches(raw, hash)).isTrue();
        assertThat(hasher.matches("wrong", hash)).isFalse();
    }
}
