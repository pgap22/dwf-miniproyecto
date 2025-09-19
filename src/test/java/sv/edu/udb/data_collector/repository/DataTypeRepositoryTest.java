package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import sv.edu.udb.data_collector.domain.DataType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DataTypeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        // Creamos y guardamos un conjunto de datos de prueba.
        // El orden de persistencia es aleatorio para asegurar que el test
        // comprueba el ordenamiento del método del repositorio.
        entityManager.persist(DataType.builder().id("dt-3").name("NUMBER").kind("number").build());
        entityManager.persist(DataType.builder().id("dt-1").name("STRING").kind("string").build());
        entityManager.persist(DataType.builder().id("dt-4").name("CATALOG").kind("catalog").build());
        entityManager.persist(DataType.builder().id("dt-2").name("BOOLEAN").kind("boolean").build());
        
        entityManager.flush();
    }

    @Test
    @DisplayName("Debe devolver todos los tipos de dato ordenados por nombre ascendentemente")
    void findAllByOrderByNameAsc_shouldReturnAllDataTypesOrdered() {
        // Arrange
        // Los datos ya fueron preparados en el método setUp().

        // Act
        List<DataType> dataTypes = dataTypeRepository.findAllByOrderByNameAsc();

        // Assert
        assertThat(dataTypes).hasSize(4);
        // isSorted() de AssertJ comprueba que la lista esté ordenada por el campo 'name'.
        assertThat(dataTypes).isSortedAccordingTo((dt1, dt2) -> dt1.getName().compareTo(dt2.getName()));
        // También podemos verificar el orden explícitamente.
        assertThat(dataTypes).extracting(DataType::getName)
                .containsExactly("BOOLEAN", "CATALOG", "NUMBER", "STRING");
    }

    @Test
    @DisplayName("Debe devolver todos los tipos de dato excepto 'CATALOG', ordenados por nombre")
    void findAllByNameNotOrderByNameAsc_shouldReturnPrimitivesOrdered() {
        // Arrange
        String excludedName = "CATALOG";

        // Act
        List<DataType> dataTypes = dataTypeRepository.findAllByNameNotOrderByNameAsc(excludedName);

        // Assert
        assertThat(dataTypes).hasSize(3);
        // Verificamos que ninguno de los resultados se llame 'CATALOG'.
        assertThat(dataTypes).noneMatch(dataType -> dataType.getName().equals(excludedName));
        // Verificamos el orden correcto de los elementos restantes.
        assertThat(dataTypes).extracting(DataType::getName)
                .containsExactly("BOOLEAN", "NUMBER", "STRING");
    }
}