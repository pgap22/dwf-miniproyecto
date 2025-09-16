package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
import sv.edu.udb.data_collector.controller.request.WorkspaceUpdateRequest;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.WorkspaceServiceImpl;
import sv.edu.udb.data_collector.service.mapper.WorkspaceMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceServiceImpl")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WorkspaceServiceImplTest {

    @Mock WorkspaceRepository repository;
    @Mock WorkspaceMapper mapper;

    @InjectMocks WorkspaceServiceImpl service;

    // ---------- create ----------

    @Test
    @DisplayName("create crea workspace cuando el nombre no existe")
    void create_creates_workspace_when_name_not_exists() {
        // Arrange
        var req = WorkspaceCreateRequest.builder().name("Data Team").build();

        var saved = Workspace.builder()
                .id("ws-1")
                .name("Data Team")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        var resp = WorkspaceResponse.builder()
                .id("ws-1")
                .name("Data Team")
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();

        when(repository.existsByNameIgnoreCase("Data Team")).thenReturn(false);
        when(repository.save(any(Workspace.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        // Act
        var result = service.create(req);

        // Assert
        assertThat(result).isEqualTo(resp);
        verify(repository).existsByNameIgnoreCase("Data Team");
        verify(repository).save(argThat(w -> w.getName().equals("Data Team")));
        verify(mapper).toResponse(saved);
    }

    @Test
    @DisplayName("create lanza 400 cuando el nombre ya existe")
    void create_throws_400_when_name_exists() {
        // Arrange
        var req = WorkspaceCreateRequest.builder().name("Data Team").build();
        when(repository.existsByNameIgnoreCase("Data Team")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Workspace name already exists");

        verify(repository).existsByNameIgnoreCase("Data Team");
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    // ---------- get ----------

    @Test
    @DisplayName("get devuelve workspace cuando existe")
    void get_returns_workspace_when_found() {
        // Arrange
        var ws = Workspace.builder().id("ws-1").name("Data").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        var resp = WorkspaceResponse.builder().id("ws-1").name("Data").createdAt(ws.getCreatedAt()).updatedAt(ws.getUpdatedAt()).build();

        when(repository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(mapper.toResponse(ws)).thenReturn(resp);

        // Act
        var result = service.get("ws-1");

        // Assert
        assertThat(result).isEqualTo(resp);
        verify(repository).findById("ws-1");
        verify(mapper).toResponse(ws);
    }

    @Test
    @DisplayName("get lanza 404 cuando no existe")
    void get_throws_404_when_not_found() {
        when(repository.findById("ws-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get("ws-x"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Workspace not found");

        verify(repository).findById("ws-x");
        verifyNoInteractions(mapper);
    }

    // ---------- list ----------

    @Test
    @DisplayName("list devuelve todos los workspaces mapeados")
    void list_returns_all_mapped() {
        // Arrange
        var ws1 = Workspace.builder().id("ws-1").name("A").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        var ws2 = Workspace.builder().id("ws-2").name("B").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        var r1 = WorkspaceResponse.builder().id("ws-1").name("A").createdAt(ws1.getCreatedAt()).updatedAt(ws1.getUpdatedAt()).build();
        var r2 = WorkspaceResponse.builder().id("ws-2").name("B").createdAt(ws2.getCreatedAt()).updatedAt(ws2.getUpdatedAt()).build();

        when(repository.findAll()).thenReturn(List.of(ws1, ws2));
        when(mapper.toResponse(ws1)).thenReturn(r1);
        when(mapper.toResponse(ws2)).thenReturn(r2);

        // Act
        var result = service.list();

        // Assert
        assertThat(result).containsExactly(r1, r2);
        verify(repository).findAll();
        verify(mapper).toResponse(ws1);
        verify(mapper).toResponse(ws2);
    }

    // ---------- patch ----------

    @Test
    @DisplayName("patch no cambia el nombre cuando viene null o en blanco")
    void patch_does_not_change_name_when_null_or_blank() {
        // Arrange
        var ws = Workspace.builder().id("ws-1").name("Original").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        var req = WorkspaceUpdateRequest.builder().name("  ").build(); // blank

        var resp = WorkspaceResponse.builder()
                .id("ws-1").name("Original").createdAt(ws.getCreatedAt()).updatedAt(ws.getUpdatedAt()).build();

        when(repository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(mapper.toResponse(ws)).thenReturn(resp);

        // Act
        var result = service.patch("ws-1", req);

        // Assert
        assertThat(ws.getName()).isEqualTo("Original");
        assertThat(result).isEqualTo(resp);

        verify(repository).findById("ws-1");
        verify(repository, never()).existsByNameIgnoreCase(anyString());
        verify(mapper).toResponse(ws);
    }

    @Test
    @DisplayName("patch cambia el nombre cuando es distinto y no existe duplicado")
    void patch_changes_name_when_different_and_not_taken() {
        // Arrange
        var ws = Workspace.builder().id("ws-1").name("Original").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        var req = WorkspaceUpdateRequest.builder().name("Nuevo").build();

        var resp = WorkspaceResponse.builder()
                .id("ws-1").name("Nuevo").createdAt(ws.getCreatedAt()).updatedAt(ws.getUpdatedAt()).build();

        when(repository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(repository.existsByNameIgnoreCase("Nuevo")).thenReturn(false);
        when(mapper.toResponse(ws)).thenReturn(resp);

        // Act
        var result = service.patch("ws-1", req);

        // Assert
        assertThat(ws.getName()).isEqualTo("Nuevo"); // dirty checking en runtime real; aquí verificamos setName
        assertThat(result).isEqualTo(resp);

        verify(repository).findById("ws-1");
        verify(repository).existsByNameIgnoreCase("Nuevo");
        verify(mapper).toResponse(ws);
    }

    @Test
    @DisplayName("patch lanza 400 cuando el nuevo nombre ya existe en otro workspace")
    void patch_throws_400_when_new_name_already_exists() {
        // Arrange
        var ws = Workspace.builder().id("ws-1").name("Original").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        var req = WorkspaceUpdateRequest.builder().name("Repetido").build();

        when(repository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(repository.existsByNameIgnoreCase("Repetido")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> service.patch("ws-1", req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Workspace name already exists");

        verify(repository).findById("ws-1");
        verify(repository).existsByNameIgnoreCase("Repetido");
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("patch lanza 404 cuando no existe el workspace")
    void patch_throws_404_when_workspace_not_found() {
        when(repository.findById("ws-x")).thenReturn(Optional.empty());

        var req = WorkspaceUpdateRequest.builder().name("Nuevo").build();

        assertThatThrownBy(() -> service.patch("ws-x", req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Workspace not found");

        verify(repository).findById("ws-x");
        verifyNoInteractions(mapper);
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete elimina cuando existe el workspace")
    void delete_deletes_when_exists() {
        when(repository.existsById("ws-1")).thenReturn(true);

        // Act
        service.delete("ws-1");

        // Assert
        verify(repository).existsById("ws-1");
        verify(repository).deleteById("ws-1");
    }

    @Test
    @DisplayName("delete lanza 404 cuando no existe el workspace")
    void delete_throws_404_when_not_found() {
        when(repository.existsById("ws-x")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("ws-x"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Workspace not found");

        verify(repository).existsById("ws-x");
        verify(repository, never()).deleteById(anyString());
    }
}
