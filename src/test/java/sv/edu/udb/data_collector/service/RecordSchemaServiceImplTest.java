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
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.RecordSchemaRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.RecordSchemaServiceImpl;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaMapper;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaMapperImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordSchemaServiceImplTest {

    @Mock
    private RecordSchemaRepository recordSchemeRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;
    
    // Usamos @Spy para los mappers porque queremos usar sus métodos reales de mapeo
    @Spy
    private RecordSchemaMapper recordSchemaMapper = Mappers.getMapper(RecordSchemaMapper.class);

    @InjectMocks
    private RecordSchemaServiceImpl recordSchemeService;

    private Workspace workspace;
    private RecordSchema recordSchema;
    private RecordSchemaResponse recordSchemaResponse;

    @BeforeEach
    void setUp() {
        workspace = Workspace.builder().id("ws-123").name("Mi Workspace").build();
        recordSchema = RecordSchema.builder()
                .id("rs-456")
                .name("Esquema de Clientes")
                .description("Datos de clientes")
                .workspace(workspace)
                .build();
        recordSchemaResponse = RecordSchemaResponse.builder()
                .id("rs-456")
                .name("Esquema de Clientes")
                .description("Datos de clientes")
                .workspaceId("ws-123")
                .build();
    }

    // --- Pruebas para el método create() ---
    @Test
    @DisplayName("Debe crear un RecordSchema exitosamente y devolver un DTO de respuesta")
    void create_whenDataIsValid_shouldReturnSavedResponseDTO() {
        // Arrange
        RecordSchemaRequestCreate createRequest = new RecordSchemaRequestCreate();
        createRequest.setWorkspaceId("ws-123");
        createRequest.setName("Nuevo Esquema");
        createRequest.setDescription("Descripción");

        when(workspaceRepository.findById("ws-123")).thenReturn(Optional.of(workspace));
        when(recordSchemeRepository.findByWorkspaceIdAndName("ws-123", "Nuevo Esquema")).thenReturn(Optional.empty());
        when(recordSchemeRepository.save(any(RecordSchema.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RecordSchemaResponse result = recordSchemeService.create(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Nuevo Esquema");
        assertThat(result.getWorkspaceId()).isEqualTo("ws-123");
        verify(recordSchemeRepository, times(1)).save(any(RecordSchema.class));
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (404) si el Workspace no existe al crear")
    void create_whenWorkspaceNotFound_shouldThrowResponseStatusException() {
        // Arrange
        RecordSchemaRequestCreate createRequest = new RecordSchemaRequestCreate();
        createRequest.setWorkspaceId("ws-inexistente");
        when(workspaceRepository.findById("ws-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            recordSchemeService.create(createRequest);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("Workspace no encontrado");
        verify(recordSchemeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException (409) si el nombre del esquema ya existe")
    void create_whenNameAlreadyExists_shouldThrowResponseStatusException() {
        // Arrange
        RecordSchemaRequestCreate createRequest = new RecordSchemaRequestCreate();
        createRequest.setWorkspaceId("ws-123");
        createRequest.setName("Nombre Repetido");
        
        when(workspaceRepository.findById("ws-123")).thenReturn(Optional.of(workspace));
        when(recordSchemeRepository.findByWorkspaceIdAndName("ws-123", "Nombre Repetido")).thenReturn(Optional.of(recordSchema));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            recordSchemeService.create(createRequest);
        });
        
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).contains("Ya existe un esquema con el nombre 'Nombre Repetido'");
    }
    
    // --- Pruebas para el método update() ---
    @Test
    @DisplayName("Debe actualizar un RecordSchema exitosamente y devolver un DTO de respuesta")
    void update_whenDataIsValid_shouldReturnUpdatedResponseDTO() {
        // Arrange
        RecordSchemaRequestUpdate updateRequest = new RecordSchemaRequestUpdate();
        updateRequest.setName("Nuevo Nombre");
        updateRequest.setDescription("Nueva Desc.");
        
        when(recordSchemeRepository.findById("rs-456")).thenReturn(Optional.of(recordSchema));
        when(recordSchemeRepository.save(any(RecordSchema.class))).thenReturn(recordSchema);

        // Act
        RecordSchemaResponse result = recordSchemeService.update("rs-456", updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        assertThat(result.getDescription()).isEqualTo("Nueva Desc.");
        verify(recordSchemeRepository).save(recordSchema);
    }
    
    @Test
    @DisplayName("Debe lanzar ResponseStatusException (404) si el RecordSchema no existe al actualizar")
    void update_whenSchemeNotFound_shouldThrowResponseStatusException() {
        // Arrange
        when(recordSchemeRepository.findById(anyString())).thenReturn(Optional.empty());
        RecordSchemaRequestUpdate updateRequest = new RecordSchemaRequestUpdate();

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            recordSchemeService.update("id-inexistente", updateRequest);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("RecordSchema no encontrado");
    }
    
    // --- Pruebas para el método delete() ---
    @Test
    @DisplayName("Debe eliminar un RecordSchema cuando existe")
    void delete_whenSchemeExists_shouldCallDelete() {
        // Arrange
        when(recordSchemeRepository.findById("rs-456")).thenReturn(Optional.of(recordSchema));
        doNothing().when(recordSchemeRepository).delete(recordSchema);

        // Act
        recordSchemeService.delete("rs-456");

        // Assert
        verify(recordSchemeRepository, times(1)).delete(recordSchema);
    }
    
    @Test
    @DisplayName("Debe lanzar ResponseStatusException (404) al intentar eliminar un RecordSchema que no existe")
    void delete_whenSchemeDoesNotExist_shouldThrowResponseStatusException() {
        // Arrange
        when(recordSchemeRepository.findById("id-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            recordSchemeService.delete("id-inexistente");
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).contains("No se puede eliminar. RecordSchema no encontrado");
        verify(recordSchemeRepository, never()).delete(any());
    }
}