package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import sv.edu.udb.data_collector.controller.request.UpdateAttributeRequest;
import sv.edu.udb.data_collector.controller.response.AttributeResponse;
import sv.edu.udb.data_collector.domain.*;

import static org.assertj.core.api.Assertions.assertThat;

class RecordSchemaAttributeMapperTest {

    // Obtenemos una instancia del mapper que MapStruct generó
    private final RecordSchemaAttributeMapper mapper = Mappers.getMapper(RecordSchemaAttributeMapper.class);

    @Test
    @DisplayName("Debe mapear la entidad a un Response correctamente, incluyendo un catálogo")
    void shouldMapEntityToResponse() {
        // Arrange (Organizar)
        // Creamos un escenario completo con todas las entidades relacionadas.
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
        AttributeResponse response = mapper.toResponse(entity);

        // Assert (Afirmar)
        // Verificamos que todos los campos se mapearon como se esperaba.
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("attr-1");
        assertThat(response.getName()).isEqualTo("Ciudad de Origen");
        assertThat(response.isRequired()).isTrue();
        assertThat(response.isAllowMultiple()).isFalse();
        // Verificamos las reglas de mapeo personalizadas
        assertThat(response.getRecordSchemaId()).isEqualTo("schema-1");
        assertThat(response.getDataTypeId()).isEqualTo("dt-text");
        assertThat(response.getCatalogId()).isEqualTo("cat-cities");
    }

    @Test
    @DisplayName("Debe mapear la entidad a un Response correctamente cuando el catálogo es nulo")
    void shouldMapEntityToResponseWhenCatalogIsNull() {
        // Arrange
        // Creamos un escenario donde el catálogo es nulo.
        RecordSchema schema = RecordSchema.builder().id("schema-1").build();
        DataType dataType = DataType.builder().id("dt-text").build();
        
        RecordSchemaAttribute entity = RecordSchemaAttribute.builder()
                .id("attr-2")
                .name("Dirección")
                .recordSchema(schema)
                .dataType(dataType)
                .catalog(null) // Catálogo es nulo
                .build();
        
        // Act
        AttributeResponse response = mapper.toResponse(entity);

        // Assert
        // Verificamos que MapStruct manejó el catálogo nulo correctamente.
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("attr-2");
        assertThat(response.getRecordSchemaId()).isEqualTo("schema-1");
        assertThat(response.getCatalogId()).isNull();
    }

    @Test
    @DisplayName("Debe actualizar una entidad existente desde un UpdateAttributeRequest")
    void shouldUpdateEntityFromRequest() {
        // Arrange
        // Creamos la entidad con sus valores originales.
        RecordSchemaAttribute existingEntity = RecordSchemaAttribute.builder()
                .id("attr-3")
                .name("Nombre Original")
                .isRequired(true)
                .recordSchema(RecordSchema.builder().id("schema-2").build()) // Datos que no deben cambiar
                .build();

        // Creamos el objeto de petición con los nuevos valores.
        UpdateAttributeRequest request = new UpdateAttributeRequest();
        request.setName("Nombre Actualizado");
        request.setIsRequired(false);
        request.setAllowMultiple(true);
        // Dejamos dataTypeId y catalogId nulos en el request.
        
        // Act
        // El método no devuelve nada, solo modifica la entidad existente.
        mapper.updateFromRequest(request, existingEntity);

        // Assert
        // Verificamos que los campos se actualizaron.
        assertThat(existingEntity.getName()).isEqualTo("Nombre Actualizado");
        assertThat(existingEntity.isRequired()).isFalse();
        assertThat(existingEntity.isAllowMultiple()).isTrue();
        // Verificamos que los campos no presentes en el request no fueron alterados.
        assertThat(existingEntity.getId()).isEqualTo("attr-3");
        assertThat(existingEntity.getRecordSchema()).isNotNull();
        assertThat(existingEntity.getRecordSchema().getId()).isEqualTo("schema-2");
    }
}