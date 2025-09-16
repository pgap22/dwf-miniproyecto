package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.domain.Workspace;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceMapperTest {

    private final WorkspaceMapper mapper = Mappers.getMapper(WorkspaceMapper.class);

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Instant updated = Instant.parse("2024-01-02T00:00:00Z");

        Workspace w = Workspace.builder()
                .id("10")                 // id es String en tu entidad
                .name("Analytics")
                .createdAt(created)
                .updatedAt(updated)
                .build();

        // Act
        WorkspaceResponse r = mapper.toResponse(w);

        // Assert
        assertNotNull(r);
        assertEquals("10", r.getId());
        assertEquals("Analytics", r.getName());
        assertEquals(created, r.getCreatedAt());
        assertEquals(updated, r.getUpdatedAt());
    }

    @Test
    void toResponse_shouldReturnNullOnNull() {
        assertNull(mapper.toResponse(null));
    }
}
