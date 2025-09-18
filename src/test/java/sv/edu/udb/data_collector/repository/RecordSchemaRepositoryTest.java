package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.domain.Workspace;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RecordSchemaRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager; // Utilidad para manipular entidades en las pruebas

    @Autowired
    private RecordSchemaRepository recordSchemeRepository; // El repositorio que vamos a probar

    private Workspace workspace;

    // Este método se ejecuta antes de cada prueba para preparar datos comunes
    @BeforeEach
    void setUp() {
        workspace = Workspace.builder().name("Mi Workspace de Prueba").build();
        testEntityManager.persist(workspace); // Guardamos el workspace para poder asociarle esquemas
    }

    @Test
    @DisplayName("Debe encontrar un RecordScheme por workspaceId y nombre cuando existe")
    void findByWorkspaceIdAndName_whenSchemeExists_thenReturnScheme() {
        // Arrange (Organizar)
        // Preparamos los datos necesarios para la prueba.
        RecordSchema scheme = RecordSchema.builder()
                .name("Esquema de Clientes")
                .description("Esquema para datos de clientes")
                .workspace(workspace)
                .build();
        testEntityManager.persistAndFlush(scheme); // Guardamos y sincronizamos con la BD

        // Act (Actuar)
        // Ejecutamos el método que queremos probar.
        Optional<RecordSchema> foundScheme = recordSchemeRepository.findByWorkspaceIdAndName(
                workspace.getId(),
                "Esquema de Clientes"
        );

        // Assert (Afirmar)
        // Verificamos que el resultado es el esperado.
        assertThat(foundScheme).isPresent();
        assertThat(foundScheme.get().getName()).isEqualTo(scheme.getName());
        assertThat(foundScheme.get().getWorkspace().getId()).isEqualTo(workspace.getId());
    }

    @Test
    @DisplayName("Debe devolver un Optional vacío si el RecordScheme no existe")
    void findByWorkspaceIdAndName_whenSchemeDoesNotExist_thenReturnEmpty() {
        // Arrange
        // No necesitamos crear ningún RecordScheme para esta prueba.

        // Act
        Optional<RecordSchema> foundScheme = recordSchemeRepository.findByWorkspaceIdAndName(
                workspace.getId(),
                "Nombre Inexistente"
        );

        // Assert
        assertThat(foundScheme).isNotPresent();
    }

    @Test
    @DisplayName("Debe encontrar todos los RecordSchemes para un workspaceId específico")
    void findByWorkspaceId_whenSchemesExist_thenReturnSchemeList() {
        // Arrange
        RecordSchema scheme1 = RecordSchema.builder().name("Esquema A").workspace(workspace).build();
        RecordSchema scheme2 = RecordSchema.builder().name("Esquema B").workspace(workspace).build();

        // Creamos otro workspace y esquema para asegurarnos de que no se mezclen los resultados
        Workspace anotherWorkspace = Workspace.builder().name("Otro Workspace").build();
        RecordSchema schemeFromAnotherWorkspace = RecordSchema.builder().name("Esquema C").workspace(anotherWorkspace).build();

        testEntityManager.persist(scheme1);
        testEntityManager.persist(scheme2);
        testEntityManager.persist(anotherWorkspace);
        testEntityManager.persistAndFlush(schemeFromAnotherWorkspace);

        // Act
        List<RecordSchema> foundSchemes = recordSchemeRepository.findByWorkspaceId(workspace.getId());

        // Assert
        assertThat(foundSchemes).isNotNull();
        assertThat(foundSchemes).hasSize(2); // Debemos encontrar solo 2
        assertThat(foundSchemes).extracting(RecordSchema::getName).containsExactlyInAnyOrder("Esquema A", "Esquema B");
    }
    
    @Test
    @DisplayName("Debe devolver una lista vacía si un workspace no tiene esquemas")
    void findByWorkspaceId_whenNoSchemesExist_thenReturnEmptyList() {
        // Arrange
        // El workspace creado en setUp() no tiene esquemas asociados por defecto.

        // Act
        List<RecordSchema> foundSchemes = recordSchemeRepository.findByWorkspaceId(workspace.getId());

        // Assert
        assertThat(foundSchemes).isNotNull();
        assertThat(foundSchemes).isEmpty();
    }
}