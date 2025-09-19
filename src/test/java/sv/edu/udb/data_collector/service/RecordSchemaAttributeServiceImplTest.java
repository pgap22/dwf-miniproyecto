package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;
import sv.edu.udb.data_collector.repository.CatalogRepository;
import sv.edu.udb.data_collector.repository.DataTypeRepository;
import sv.edu.udb.data_collector.repository.RecordSchemaAttributeRepository;
import sv.edu.udb.data_collector.repository.RecordSchemaRepository;
import sv.edu.udb.data_collector.service.implementation.RecordSchemaAttributeServiceImpl;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaAttributeMapper;

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
    
    // Inyectamos el mapper real usando @Spy
    @Spy
    private RecordSchemaAttributeMapper attributeMapper = Mappers.getMapper(RecordSchemaAttributeMapper.class);

    @InjectMocks
    private RecordSchemaAttributeServiceImpl attributeService;

    // Variables de prueba
    private RecordSchema schema;
    private DataType dataType;
    private RecordSchemaAttributeCreateRequest createRequest;
    private RecordSchemaAttribute savedAttribute;

    @BeforeEach
    void setUp() {
        // Pre-configuramos objetos que usaremos en múltiples pruebas
        schema = RecordSchema.builder().id("schema-1").name("Test Schema").build();
        dataType = DataType.builder().id("dt-text").name("Texto").kind("TEXT").build();
        savedAttribute = RecordSchemaAttribute.builder()
                .id("attr-1")
                .name("Nuevo Atributo")
                .isRequired(true)
                .allowMultiple(false)
                .recordSchema(schema)
                .dataType(dataType)
                .build();
        
        createRequest = new RecordSchemaAttributeCreateRequest();
        createRequest.setName("Nuevo Atributo");
        createRequest.setIsRequired(true);
        createRequest.setAllowMultiple(false);
        createRequest.setDataTypeId("dt-text");
        createRequest.setCatalogId(null);
    }

    @Test
    @DisplayName("Debe añadir un atributo a un esquema exitosamente y devolver un DTO de respuesta")
    void addAttributeToSchema_whenDataIsValid_shouldSucceed() {
        // Arrange (Organizar)
        given(schemaRepository.findById("schema-1")).willReturn(Optional.of(schema));
        given(attributeRepository.findByRecordSchemaIdAndName("schema-1", "Nuevo Atributo")).willReturn(Optional.empty());
        given(dataTypeRepository.findById("dt-text")).willReturn(Optional.of(dataType));
        given(attributeRepository.save(any(RecordSchemaAttribute.class))).willAnswer(invocation -> savedAttribute);

        // Act (Actuar)
        RecordSchemaAttributeResponse result = attributeService.add("schema-1", createRequest);

        // Assert (Afirmar)
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Nuevo Atributo");
        assertThat(result.getRecordSchemaId()).isEqualTo("schema-1");
        assertThat(result.getDataTypeId()).isEqualTo("dt-text");
        assertThat(result.getCatalogId()).isNull();
        verify(attributeRepository, times(1)).save(any(RecordSchemaAttribute.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (404) si el RecordSchema no existe")
    void addAttributeToSchema_whenSchemaNotFound_shouldThrowException() {
        // Arrange
        given(schemaRepository.findById("schema-inexistente")).willReturn(Optional.empty());
        
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            attributeService.add("schema-inexistente", createRequest);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("RecordSchema no encontrado");
        verify(attributeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (409) si el nombre del atributo ya existe")
    void addAttributeToSchema_whenNameIsDuplicate_shouldThrowException() {
        // Arrange
        given(schemaRepository.findById("schema-1")).willReturn(Optional.of(schema));
        given(attributeRepository.findByRecordSchemaIdAndName("schema-1", "Nuevo Atributo"))
                .willReturn(Optional.of(new RecordSchemaAttribute()));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            attributeService.add("schema-1", createRequest);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).contains("ya existe en este esquema");
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
        given(catalogRepository.findById("cat-1")).willReturn(Optional.of(catalog));
        given(attributeRepository.save(any(RecordSchemaAttribute.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act
        RecordSchemaAttributeResponse result = attributeService.add("schema-1", createRequest);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCatalogId()).isEqualTo("cat-1");
    }
}