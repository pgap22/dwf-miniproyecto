package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.domain.Workspace;


import static org.assertj.core.api.Assertions.assertThat;

class RecordSchemaMapperTest {

    // Obtenemos una instancia del mapper que MapStruct generó
    private final RecordSchemaMapper mapper = Mappers.getMapper(RecordSchemaMapper.class);

    @Test
    @DisplayName("Debe mapear una entidad RecordSchema a un RecordSchemaResponse correctamente")
    void shouldMapEntityToResponse() {
        // Arrange
        Workspace workspace = Workspace.builder()
                .id("ws-123")
                .name("Test Workspace")
                .build();

        RecordSchema entity = RecordSchema.builder()
                .id("rs-456")
                .name("Esquema de Prueba")
                .description("Descripción de prueba")
                .workspace(workspace)
                .build();

        // Act
        RecordSchemaResponse dto = mapper.toResponse(entity);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("rs-456");
        assertThat(dto.getName()).isEqualTo("Esquema de Prueba");
        assertThat(dto.getDescription()).isEqualTo("Descripción de prueba");
        assertThat(dto.getWorkspaceId()).isEqualTo("ws-123");
    }

    @Test
    @DisplayName("Debe mapear un DTO de creación a una entidad RecordSchema")
    void shouldMapCreateRequestToEntity() {
        // Arrange
        RecordSchemaRequestCreate request = new RecordSchemaRequestCreate();
        request.setName("Nuevo Esquema");
        request.setDescription("Nueva Descripción");
        request.setWorkspaceId("ws-987");

        // Act
        RecordSchema entity = mapper.toRecordSchema(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Nuevo Esquema");
        assertThat(entity.getDescription()).isEqualTo("Nueva Descripción");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getWorkspace()).isNull();
    }

    @Test
    @DisplayName("Debe actualizar una entidad existente desde un DTO de actualización")
    void shouldUpdateEntityFromRequestDTO() {
        // Arrange
        RecordSchema existingEntity = RecordSchema.builder()
                .id("rs-789")
                .name("Nombre Antiguo")
                .description("Descripción Antigua")
                .workspace(Workspace.builder().id("ws-abc").build())
                .build();

        RecordSchemaRequestUpdate requestDTO = new RecordSchemaRequestUpdate();
        requestDTO.setName("Nombre Actualizado");
        requestDTO.setDescription("Descripción Actualizada");

        // Act
        mapper.updateRecordSchema(requestDTO, existingEntity);

        // Assert
        assertThat(existingEntity.getName()).isEqualTo("Nombre Actualizado");
        assertThat(existingEntity.getDescription()).isEqualTo("Descripción Actualizada");
        assertThat(existingEntity.getId()).isEqualTo("rs-789");
        assertThat(existingEntity.getWorkspace().getId()).isEqualTo("ws-abc");
    }

    @Test
    @DisplayName("Debe devolver null si la entidad de entrada es null")
    void shouldReturnNullWhenEntityIsNull() {
        // Arrange
        RecordSchema entity = null;

        // Act
        RecordSchemaResponse dto = mapper.toResponse(entity);

        // Assert
        assertThat(dto).isNull();
    }
}