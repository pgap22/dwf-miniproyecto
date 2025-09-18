package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sv.edu.udb.data_collector.controller.request.UpdateRecordSchemeRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemeResponse;
import sv.edu.udb.data_collector.domain.RecordScheme;
import sv.edu.udb.data_collector.domain.Workspace;


import static org.assertj.core.api.Assertions.assertThat;

class RecordSchemeMapperTest {

    // Obtenemos una instancia del mapper que MapStruct generó
    private final RecordSchemeMapper mapper = RecordSchemeMapper.INSTANCE;

    @Test
    @DisplayName("Debe mapear una entidad RecordScheme a un RecordSchemeResponse correctamente")
    void shouldMapEntityToResponseDTO() {
        // Arrange (Organizar)
        // Creamos los objetos de dominio que servirán como fuente de datos.
        Workspace workspace = Workspace.builder()
                .id("ws-123")
                .name("Test Workspace")
                .build();
        
        RecordScheme entity = RecordScheme.builder()
                .id("rs-456")
                .name("Esquema de Prueba")
                .description("Descripción de prueba")
                .workspace(workspace)
                .build();

        // Act (Actuar)
        // Ejecutamos el método de mapeo que queremos probar.
        RecordSchemeResponse dto = mapper.toResponseDTO(entity);

        // Assert (Afirmar)
        // Verificamos que cada campo del DTO resultante tenga el valor esperado.
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("rs-456");
        assertThat(dto.getName()).isEqualTo("Esquema de Prueba");
        assertThat(dto.getDescription()).isEqualTo("Descripción de prueba");
        // La prueba más importante: verificar que la regla personalizada @Mapping funciona.
        assertThat(dto.getWorkspaceId()).isEqualTo("ws-123"); 
    }

    @Test
    @DisplayName("Debe actualizar una entidad existente desde un DTO de actualización")
    void shouldUpdateEntityFromRequestDTO() {
        // Arrange
        // Creamos una entidad con datos originales.
        RecordScheme existingEntity = RecordScheme.builder()
                .id("rs-789")
                .name("Nombre Antiguo")
                .description("Descripción Antigua")
                .workspace(Workspace.builder().id("ws-abc").build())
                .build();
        
        // Creamos el DTO con los datos nuevos.
        UpdateRecordSchemeRequest requestDTO = new UpdateRecordSchemeRequest();
        requestDTO.setName("Nombre Actualizado");
        requestDTO.setDescription("Descripción Actualizada");

        // Act
        // Ejecutamos el método de actualización. Este método no devuelve nada, modifica el objeto existente.
        mapper.updateFromRequest(requestDTO, existingEntity);

        // Assert
        // Verificamos que los campos de la entidad original hayan sido modificados.
        assertThat(existingEntity.getName()).isEqualTo("Nombre Actualizado");
        assertThat(existingEntity.getDescription()).isEqualTo("Descripción Actualizada");
        // También verificamos que los campos que no estaban en el DTO no hayan cambiado.
        assertThat(existingEntity.getId()).isEqualTo("rs-789");
        assertThat(existingEntity.getWorkspace().getId()).isEqualTo("ws-abc");
    }
    
    @Test
    @DisplayName("Debe devolver null si la entidad de entrada es null")
    void shouldReturnNullWhenEntityIsNull() {
        // Arrange
        RecordScheme entity = null;
        
        // Act
        RecordSchemeResponse dto = mapper.toResponseDTO(entity);
        
        // Assert
        assertThat(dto).isNull();
    }
}