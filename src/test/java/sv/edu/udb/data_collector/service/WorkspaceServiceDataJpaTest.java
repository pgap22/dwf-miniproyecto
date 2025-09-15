package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
import sv.edu.udb.data_collector.controller.request.WorkspaceUpdateRequest;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.WorkspaceServiceImpl;
import sv.edu.udb.data_collector.service.mapper.WorkspaceMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({WorkspaceServiceImpl.class, WorkspaceMapper.class})
@ActiveProfiles("test")
class WorkspaceServiceDataJpaTest {

    @Autowired private WorkspaceService workspaceService;
    @Autowired private WorkspaceRepository workspaceRepository;

    @BeforeEach
    void seed() {
        // Arrange
        WorkspaceCreateRequest req = new WorkspaceCreateRequest();
        req.setName("Anything you want to write");
        workspaceService.create(req);
    }

    @AfterEach
    void clean() {
        workspaceRepository.deleteAll();
    }

    @Test
    void list_shouldReturnOne() {
        // Act
        List<WorkspaceResponse> list = workspaceService.list();
        // Assert
        assertEquals(1, list.size());
        assertEquals("Anything you want to write", list.get(0).getName());
    }

    @Test
    void get_shouldReturnById() {
        // Arrange
        String id = workspaceRepository.findAll().get(0).getId();
        // Act
        WorkspaceResponse resp = workspaceService.get(id);
        // Assert
        assertNotNull(resp);
        assertEquals(id, resp.getId());
        assertEquals("Anything you want to write", resp.getName());
    }

    @Test
    void create_shouldWork() {
        // Arrange
        WorkspaceCreateRequest req = new WorkspaceCreateRequest();
        req.setName("Second WS");
        // Act
        WorkspaceResponse created = workspaceService.create(req);
        // Assert
        assertNotNull(created.getId());
        assertEquals("Second WS", created.getName());
    }

    @Test
    void create_shouldFailOnDuplicateName() {
        // Arrange
        WorkspaceCreateRequest dup = new WorkspaceCreateRequest();
        dup.setName("Anything you want to write"); // case-insensitive en el service
        // Act + Assert
        assertThrows(ResponseStatusException.class, () -> workspaceService.create(dup));
    }

    @Test
    void patch_shouldUpdateName() {
        // Arrange
        String id = workspaceRepository.findAll().get(0).getId();
        WorkspaceUpdateRequest up = new WorkspaceUpdateRequest();
        up.setName("New Name");
        // Act
        WorkspaceResponse updated = workspaceService.patch(id, up);
        // Assert
        assertEquals("New Name", updated.getName());
        assertEquals(id, updated.getId());
    }

    @Test
    void delete_shouldRemove() {
        // Arrange
        String id = workspaceRepository.findAll().get(0).getId();
        // Act
        workspaceService.delete(id);
        // Assert
        assertTrue(workspaceRepository.findById(id).isEmpty());
    }

    @Test
    void delete_shouldThrowNotFound() {
        // Act + Assert
        assertThrows(ResponseStatusException.class, () -> workspaceService.delete("does-not-exist"));
    }
}
