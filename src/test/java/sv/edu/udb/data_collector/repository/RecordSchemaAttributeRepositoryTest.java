package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import sv.edu.udb.data_collector.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RecordSchemaAttributeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecordSchemaAttributeRepository attributeRepository;

    // Entidades de prerrequisito que crearemos antes de cada prueba
    private Workspace workspace;
    private RecordSchema schema1;
    private RecordSchema schema2;
    private DataType dataType;
    private Catalog catalog;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común para todas las pruebas)
        workspace = Workspace.builder().name("Test Workspace").build();
        entityManager.persist(workspace);
        
        schema1 = RecordSchema.builder().name("Schema 1").workspace(workspace).build();
        schema2 = RecordSchema.builder().name("Schema 2").workspace(workspace).build();
        entityManager.persist(schema1);
        entityManager.persist(schema2);

        dataType = DataType.builder().name("Texto").kind("string").build();
        entityManager.persist(dataType);
        
        catalog = Catalog.builder().name("Ciudades").workspace(workspace).build();
        entityManager.persist(catalog);

        // Creamos algunos atributos para las pruebas
        RecordSchemaAttribute attr1 = RecordSchemaAttribute.builder().name("Nombre").recordSchema(schema1).dataType(dataType).build();
        RecordSchemaAttribute attr2 = RecordSchemaAttribute.builder().name("Apellido").recordSchema(schema1).dataType(dataType).build();
        RecordSchemaAttribute attr3 = RecordSchemaAttribute.builder().name("Ciudad").recordSchema(schema1).dataType(dataType).catalog(catalog).build();
        RecordSchemaAttribute attr4 = RecordSchemaAttribute.builder().name("Dirección").recordSchema(schema2).dataType(dataType).build();
        
        entityManager.persist(attr1);
        entityManager.persist(attr2);
        entityManager.persist(attr3);
        entityManager.persist(attr4);
        
        entityManager.flush(); // Sincroniza la BD en memoria
    }

    @Test
    @DisplayName("Debe encontrar todos los atributos por recordSchemaId")
    void findByRecordSchemaId_shouldReturnAttributesForGivenSchema() {
        // Arrange
        // Los datos ya fueron preparados en el método setUp()

        // Act
        List<RecordSchemaAttribute> foundAttributes = attributeRepository.findByRecordSchemaId(schema1.getId());

        // Assert
        assertThat(foundAttributes).hasSize(3);
        assertThat(foundAttributes).extracting(RecordSchemaAttribute::getName)
                .containsExactlyInAnyOrder("Nombre", "Apellido", "Ciudad");
    }

    @Test
    @DisplayName("Debe encontrar un atributo por recordSchemaId y nombre")
    void findByRecordSchemaIdAndName_whenExists_shouldReturnAttribute() {
        // Arrange
        // Los datos ya fueron preparados

        // Act
        Optional<RecordSchemaAttribute> foundAttribute = attributeRepository.findByRecordSchemaIdAndName(schema1.getId(), "Nombre");

        // Assert
        assertThat(foundAttribute).isPresent();
        assertThat(foundAttribute.get().getName()).isEqualTo("Nombre");
        assertThat(foundAttribute.get().getRecordSchema().getId()).isEqualTo(schema1.getId());
    }

    @Test
    @DisplayName("No debe encontrar un atributo si el nombre no existe en el esquema")
    void findByRecordSchemaIdAndName_whenNameDoesNotExist_shouldReturnEmpty() {
        // Arrange
        // Los datos ya fueron preparados

        // Act
        Optional<RecordSchemaAttribute> foundAttribute = attributeRepository.findByRecordSchemaIdAndName(schema1.getId(), "NombreInexistente");

        // Assert
        assertThat(foundAttribute).isNotPresent();
    }
    
    @Test
    @DisplayName("Debe encontrar todos los atributos asociados a un catalogId")
    void findByCatalogId_shouldReturnAttributesForGivenCatalog() {
        // Arrange
        // Los datos ya fueron preparados

        // Act
        List<RecordSchemaAttribute> foundAttributes = attributeRepository.findByCatalogId(catalog.getId());

        // Assert
        assertThat(foundAttributes).hasSize(1);
        assertThat(foundAttributes.get(0).getName()).isEqualTo("Ciudad");
    }
}