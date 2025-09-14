package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.Test;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.domain.Workspace;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceMapperTest {

    private final WorkspaceMapper mapper = new WorkspaceMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        Workspace w = Workspace.builder()
                .id(10L)
                .name("Analytics")
                .description("Desc")
                .build();

        WorkspaceResponse r = mapper.toResponse(w);

        assertNotNull(r);
        assertEquals(10L, r.getId());
        assertEquals("Analytics", r.getName());
        assertEquals("Desc", r.getDescription());
    }

    @Test
    void toResponse_shouldReturnNullOnNull() {
        assertNull(mapper.toResponse(null));
    }
}
