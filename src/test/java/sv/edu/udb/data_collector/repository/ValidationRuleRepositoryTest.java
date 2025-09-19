package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import sv.edu.udb.data_collector.domain.ValidationRule;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ValidationRuleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ValidationRuleRepository validationRuleRepository;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        ValidationRule rule1 = ValidationRule.builder().name("REQUIRED").build();
        ValidationRule rule2 = ValidationRule.builder().name("MAX_LENGTH").build();
        entityManager.persist(rule1);
        entityManager.persist(rule2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Debe encontrar una regla por su nombre, ignorando mayúsculas/minúsculas")
    void findByNameIgnoreCase_whenExists_shouldReturnRule() {
        // Arrange
        // Los datos ya fueron preparados en setUp()

        // Act
        Optional<ValidationRule> foundRule = validationRuleRepository.findByNameIgnoreCase("required"); // Búsqueda en minúsculas

        // Assert
        assertThat(foundRule).isPresent();
        assertThat(foundRule.get().getName()).isEqualTo("REQUIRED");
    }

    @Test
    @DisplayName("No debe encontrar una regla si el nombre no existe")
    void findByNameIgnoreCase_whenDoesNotExist_shouldReturnEmpty() {
        // Arrange
        // Los datos ya fueron preparados

        // Act
        Optional<ValidationRule> notFoundRule = validationRuleRepository.findByNameIgnoreCase("non_existent_rule");

        // Assert
        assertThat(notFoundRule).isNotPresent();
    }

    @Test
    @DisplayName("Debe devolver true si una regla existe, ignorando mayúsculas/minúsculas")
    void existsByNameIgnoreCase_whenExists_shouldReturnTrue() {
        // Arrange
        // Los datos ya fueron preparados

        // Act
        boolean exists = validationRuleRepository.existsByNameIgnoreCase("max_length"); // Búsqueda en minúsculas

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe devolver false si una regla no existe")
    void existsByNameIgnoreCase_whenDoesNotExist_shouldReturnFalse() {
        // Arrange
        // Los datos ya fueron preparados

        // Act
        boolean exists = validationRuleRepository.existsByNameIgnoreCase("non_existent_rule");

        // Assert
        assertThat(exists).isFalse();
    }
}