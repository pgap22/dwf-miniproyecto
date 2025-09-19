package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeUpdateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;
import sv.edu.udb.data_collector.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class RecordSchemaAttributeMapperTest {

    // Obtenemos una instancia del mapper que MapStruct generó
    private final RecordSchemaAttributeMapper mapper = Mappers.getMapper(RecordSchemaAttributeMapper.class);

    @Test
    @DisplayName("Debe mapear la entidad a un Response correctamente, incluyendo un catálogo")
    void shouldMapEntityToResponseWithCatalog() {
        // Arrange (Organizar)
        RecordSchema schema = RecordSchema.builder().id("schema-1").build();
        DataType dataType = DataType.builder().id("dt-text").build();
        Catalog catalog = Catalog.builder().id("cat-cities").build();

        RecordSchemaAttribute entity = RecordSchemaAttribute.builder()
                .id("attr-1")
                .name("Ciudad de Origen")
                .isRequired(true)
                .allowMultiple(false)
                .recordSchema(schema)
                .dataType(dataType)
                .catalog(catalog)
                .build();

        // Act (Actuar)
        RecordSchemaAttributeResponse response = mapper.toResponse(entity);

        

        // Assert (Afirmar)
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("attr-1");
        assertThat(response.getName()).isEqualTo("Ciudad de Origen");
        assertThat(response.getIsRequired()).isTrue();
        assertThat(response.getAllowMultiple()).isFalse();
        assertThat(response.getRecordSchemaId()).isEqualTo("schema-1");
        assertThat(response.getDataTypeId()).isEqualTo("dt-text");
        assertThat(response.getCatalogId()).isEqualTo("cat-cities");
    }

    @Test
    @DisplayName("Debe mapear la entidad a un Response correctamente cuando el catálogo es nulo")
    void shouldMapEntityToResponseWhenCatalogIsNull() {
        // Arrange
        RecordSchema schema = RecordSchema.builder().id("schema-1").build();
        DataType dataType = DataType.builder().id("dt-text").build();

        RecordSchemaAttribute entity = RecordSchemaAttribute.builder()
                .id("attr-2")
                .name("Dirección")
                .isRequired(false)
                .allowMultiple(false)
                .recordSchema(schema)
                .dataType(dataType)
                .catalog(null)
                .build();

        // Act
        RecordSchemaAttributeResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("attr-2");
        assertThat(response.getRecordSchemaId()).isEqualTo("schema-1");
        assertThat(response.getCatalogId()).isNull();
    }
    
    @Test
    @DisplayName("Debe mapear una lista de entidades a una lista de DTOs de respuesta")
    void shouldMapEntityListToResponseList() {
        // Arrange
        RecordSchemaAttribute attr1 = RecordSchemaAttribute.builder().id("attr-1").name("name1").build();
        RecordSchemaAttribute attr2 = RecordSchemaAttribute.builder().id("attr-2").name("name2").build();
        List<RecordSchemaAttribute> entities = List.of(attr1, attr2);

        // Act
        List<RecordSchemaAttributeResponse> responseList = mapper.toResponseList(entities);

        // Assert
        assertThat(responseList).isNotNull().hasSize(2);
        assertThat(responseList)
                .extracting(RecordSchemaAttributeResponse::getId, RecordSchemaAttributeResponse::getName)
                .containsExactlyInAnyOrder(
                        tuple("attr-1", "name1"),
                        tuple("attr-2", "name2")
                );
    }
    
    @Test
    @DisplayName("Debe mapear un DTO de creación a una entidad correctamente")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        RecordSchemaAttributeCreateRequest request = new RecordSchemaAttributeCreateRequest();
        request.setName("new_attribute");
        request.setIsRequired(true);
        request.setAllowMultiple(false);
        request.setDataTypeId("dt-2");
        request.setCatalogId("cat-3");

        // Act
        RecordSchemaAttribute entity = mapper.toRecordSchemaAttribute(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("new_attribute");
        assertThat(entity.getIsRequired()).isTrue();
        assertThat(entity.getAllowMultiple()).isFalse();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getRecordSchema()).isNull();
        assertThat(entity.getDataType()).isNull();
        assertThat(entity.getCatalog()).isNull();
    }

    @Test
    @DisplayName("Debe actualizar una entidad existente desde un UpdateAttributeRequest")
    void shouldUpdateEntityFromRequest() {
        // Arrange
        RecordSchemaAttribute existingEntity = RecordSchemaAttribute.builder()
                .id("attr-3")
                .name("Nombre Original")
                .isRequired(true)
                .recordSchema(RecordSchema.builder().id("schema-2").build()) // Datos que no deben cambiar
                .build();

        RecordSchemaAttributeUpdateRequest request = new RecordSchemaAttributeUpdateRequest();
        request.setName("Nombre Actualizado");
        request.setIsRequired(false);
        request.setAllowMultiple(true);

        // Act
        mapper.updateFromRequest(request, existingEntity);

        // Assert
        assertThat(existingEntity.getName()).isEqualTo("Nombre Actualizado");
        assertThat(existingEntity.getIsRequired()).isFalse();
        assertThat(existingEntity.getAllowMultiple()).isTrue();
        assertThat(existingEntity.getId()).isEqualTo("attr-3");
        assertThat(existingEntity.getRecordSchema()).isNotNull();
        assertThat(existingEntity.getRecordSchema().getId()).isEqualTo("schema-2");
    }
}