package sv.edu.udb.data_collector.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.data_collector.controller.request.CreateAttributeRequest;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;
import sv.edu.udb.data_collector.repository.CatalogRepository;
import sv.edu.udb.data_collector.repository.DataTypeRepository;
import sv.edu.udb.data_collector.repository.RecordSchemaAttributeRepository;
import sv.edu.udb.data_collector.repository.RecordSchemaRepository;
import sv.edu.udb.data_collector.service.implementation.RecordSchemaAttributeServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordSchemaAttributeServiceImplTest {

    @Mock
    private RecordSchemaAttributeRepository attributeRepository;
    @Mock
    private RecordSchemaRepository schemaRepository;
    @Mock
    private DataTypeRepository dataTypeRepository;
    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private RecordSchemaAttributeServiceImpl attributeService;

    // Variables de prueba
    private RecordSchema schema;
    private DataType dataType;
    private CreateAttributeRequest createRequest;

    @BeforeEach
    void setUp() {
        // Pre-configuramos objetos que usaremos en múltiples pruebas
        schema = RecordSchema.builder().id("schema-1").name("Test Schema").build();
        dataType = DataType.builder().id("dt-text").name("Texto").build();
        
        createRequest = new CreateAttributeRequest();
        createRequest.setName("Nuevo Atributo");
        createRequest.setIsRequired(true);
        createRequest.setAllowMultiple(false);
        createRequest.setDataTypeId("dt-text");
        createRequest.setCatalogId(null);
    }

    @Test
    @DisplayName("Debe añadir un atributo a un esquema exitosamente")
    void addAttributeToSchema_whenDataIsValid_shouldSucceed() {
        // Arrange (Organizar)
        // Simulamos el comportamiento de los repositorios para el caso de éxito.
        given(schemaRepository.findById("schema-1")).willReturn(Optional.of(schema));
        given(attributeRepository.findByRecordSchemaIdAndName("schema-1", "Nuevo Atributo")).willReturn(Optional.empty());
        given(dataTypeRepository.findById("dt-text")).willReturn(Optional.of(dataType));
        // Simulamos que el método save devuelve el objeto que se le pasa.
        given(attributeRepository.save(any(RecordSchemaAttribute.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act (Actuar)
        // Ejecutamos el método del servicio que queremos probar.
        RecordSchemaAttribute result = attributeService.addAttributeToSchema("schema-1", createRequest);

        // Assert (Afirmar)
        // Verificamos que el resultado es el esperado y que se llamó a los métodos correctos.
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Nuevo Atributo");
        assertThat(result.getRecordSchema().getId()).isEqualTo("schema-1");
        verify(attributeRepository, times(1)).save(any(RecordSchemaAttribute.class));
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el RecordSchema no existe")
    void addAttributeToSchema_whenSchemaNotFound_shouldThrowException() {
        // Arrange
        // Simulamos que el schemaRepository no encuentra nada.
        given(schemaRepository.findById("schema-inexistente")).willReturn(Optional.empty());
        
        // Act & Assert
        // Verificamos que se lanza la excepción correcta.
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            attributeService.addAttributeToSchema("schema-inexistente", createRequest);
        });

        assertThat(exception.getMessage()).contains("RecordSchema no encontrado");
        verify(attributeRepository, never()).save(any()); // Verificamos que NUNCA se intentó guardar.
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si el nombre del atributo ya existe")
    void addAttributeToSchema_whenNameIsDuplicate_shouldThrowException() {
        // Arrange
        // Simulamos que el schema sí existe.
        given(schemaRepository.findById("schema-1")).willReturn(Optional.of(schema));
        // Simulamos que el repositorio de atributos SÍ encuentra un atributo con el mismo nombre.
        given(attributeRepository.findByRecordSchemaIdAndName("schema-1", "Nuevo Atributo"))
                .willReturn(Optional.of(new RecordSchemaAttribute()));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attributeService.addAttributeToSchema("schema-1", createRequest);
        });

        assertThat(exception.getMessage()).contains("ya existe en este esquema");
        verify(attributeRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Debe añadir un atributo con catálogo si se proporciona un catalogId válido")
    void addAttributeToSchema_withValidCatalogId_shouldSucceed() {
        // Arrange
        Catalog catalog = Catalog.builder().id("cat-1").name("Ciudades").build();
        createRequest.setCatalogId("cat-1");

        given(schemaRepository.findById("schema-1")).willReturn(Optional.of(schema));
        given(attributeRepository.findByRecordSchemaIdAndName(anyString(), anyString())).willReturn(Optional.empty());
        given(dataTypeRepository.findById("dt-text")).willReturn(Optional.of(dataType));
        given(catalogRepository.findById("cat-1")).willReturn(Optional.of(catalog)); // Simulamos que el catálogo existe
        given(attributeRepository.save(any(RecordSchemaAttribute.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act
        RecordSchemaAttribute result = attributeService.addAttributeToSchema("schema-1", createRequest);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCatalog()).isNotNull();
        assertThat(result.getCatalog().getId()).isEqualTo("cat-1");
    }
}