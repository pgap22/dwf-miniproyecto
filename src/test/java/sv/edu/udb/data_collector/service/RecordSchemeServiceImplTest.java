package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.udb.data_collector.domain.RecordScheme;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.RecordSchemeRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.RecordSchemeServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita las anotaciones de Mockito
class RecordSchemeServiceImplTest {

    @Mock // Mockito creará una implementación falsa de este repositorio
    private RecordSchemeRepository recordSchemeRepository;

    @Mock // Y de este también
    private WorkspaceRepository workspaceRepository;

    @InjectMocks // Mockito inyectará los mocks de arriba en esta instancia del servicio
    private RecordSchemeServiceImpl recordSchemeService;

    private Workspace workspace;
    private RecordScheme recordScheme;

    @BeforeEach
    void setUp() {
        // Pre-configuramos objetos de dominio que usaremos en varias pruebas
        workspace = Workspace.builder().id("ws-123").name("Mi Workspace").build();
        recordScheme = RecordScheme.builder()
                .id("rs-456")
                .name("Esquema de Clientes")
                .description("Datos de clientes")
                .workspace(workspace)
                .build();
    }

    // --- Pruebas para el método create() ---

    @Test
    @DisplayName("Debe crear un RecordScheme exitosamente")
    void create_whenDataIsValid_shouldReturnSavedScheme() {
        // Arrange (Organizar)
        // Simulamos que el workspace existe
        when(workspaceRepository.findById("ws-123")).thenReturn(Optional.of(workspace));
        // Simulamos que no existe un esquema con el mismo nombre
        when(recordSchemeRepository.findByWorkspaceIdAndName("ws-123", "Nuevo Esquema")).thenReturn(Optional.empty());
        // Simulamos lo que devolverá el método save
        when(recordSchemeRepository.save(any(RecordScheme.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Actuar)
        RecordScheme createdScheme = recordSchemeService.create("ws-123", "Nuevo Esquema", "Descripción");

        // Assert (Afirmar)
        assertThat(createdScheme).isNotNull();
        assertThat(createdScheme.getName()).isEqualTo("Nuevo Esquema");
        assertThat(createdScheme.getWorkspace().getId()).isEqualTo("ws-123");
        verify(recordSchemeRepository, times(1)).save(any(RecordScheme.class)); // Verificamos que se llamó a save()
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el Workspace no existe al crear")
    void create_whenWorkspaceNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        when(workspaceRepository.findById("ws-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            recordSchemeService.create("ws-inexistente", "Nombre", "Desc");
        });

        assertThat(exception.getMessage()).isEqualTo("Workspace no encontrado con el id: ws-inexistente");
        verify(recordSchemeRepository, never()).save(any()); // Verificamos que NUNCA se intentó guardar
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si el nombre del esquema ya existe en el workspace")
    void create_whenNameAlreadyExists_shouldThrowIllegalStateException() {
        // Arrange
        when(workspaceRepository.findById("ws-123")).thenReturn(Optional.of(workspace));
        when(recordSchemeRepository.findByWorkspaceIdAndName("ws-123", "Nombre Repetido")).thenReturn(Optional.of(recordScheme));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            recordSchemeService.create("ws-123", "Nombre Repetido", "Desc");
        });
        
        assertThat(exception.getMessage()).isEqualTo("Ya existe un esquema con el nombre 'Nombre Repetido' en este workspace.");
    }
    
    // --- Pruebas para el método update() ---

    @Test
    @DisplayName("Debe actualizar un RecordScheme exitosamente")
    void update_whenDataIsValid_shouldReturnUpdatedScheme() {
        // Arrange
        when(recordSchemeRepository.findById("rs-456")).thenReturn(Optional.of(recordScheme));
        when(recordSchemeRepository.save(any(RecordScheme.class))).thenReturn(recordScheme);

        RecordScheme updatedData = RecordScheme.builder().name("Nuevo Nombre").description("Nueva Desc.").build();

        // Act
        RecordScheme result = recordSchemeService.update("rs-456", updatedData);

        // Assert
        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        assertThat(result.getDescription()).isEqualTo("Nueva Desc.");
        verify(recordSchemeRepository).save(recordScheme);
    }
    
    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el RecordScheme no existe al actualizar")
    void update_whenSchemeNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        when(recordSchemeRepository.findById(anyString())).thenReturn(Optional.empty());
        RecordScheme updatedData = RecordScheme.builder().name("data").build();

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            recordSchemeService.update("id-inexistente", updatedData);
        });
    }

    // --- Pruebas para el método delete() ---

    @Test
    @DisplayName("Debe eliminar un RecordScheme cuando existe")
    void delete_whenSchemeExists_shouldCallDeleteById() {
        // Arrange
        when(recordSchemeRepository.existsById("rs-456")).thenReturn(true);
        doNothing().when(recordSchemeRepository).deleteById("rs-456"); // No hace nada cuando se llama a deleteById

        // Act
        recordSchemeService.delete("rs-456");

        // Assert
        verify(recordSchemeRepository, times(1)).deleteById("rs-456");
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException al intentar eliminar un RecordScheme que no existe")
    void delete_whenSchemeDoesNotExist_shouldThrowEntityNotFoundException() {
        // Arrange
        when(recordSchemeRepository.existsById("id-inexistente")).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            recordSchemeService.delete("id-inexistente");
        });
        verify(recordSchemeRepository, never()).deleteById(anyString()); // Verificamos que NUNCA se llamó a delete
    }
}