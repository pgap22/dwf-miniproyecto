package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
// import sv.edu.udb.data_collector.controller.request.WorkspaceUpdateRequest;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.WorkspaceServiceImpl;
import sv.edu.udb.data_collector.service.mapper.WorkspaceMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.flyway.enabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({WorkspaceServiceImpl.class, WorkspaceMapper.class})
@ActiveProfiles("test")
class WorkspaceServiceDataJpaTest {

    private final WorkspaceService workspaceService;
    private final WorkspaceRepository workspaceRepository;

    WorkspaceServiceDataJpaTest(WorkspaceService workspaceService,
                                WorkspaceRepository workspaceRepository) {
        this.workspaceService = workspaceService;
        this.workspaceRepository = workspaceRepository;
    }

    @BeforeEach
    void seed() {
        WorkspaceCreateRequest req = new WorkspaceCreateRequest();
        req.setName("Anything you want to write");
        req.setDescription("Initial description");
        workspaceService.create(req);
    }

    @AfterEach
    void clean() {
        workspaceRepository.deleteAll();
    }

    @Test
    void list_shouldReturnOne() {
        List<WorkspaceResponse> list = workspaceService.list();
        assertEquals(1, list.size());
        assertEquals("Anything you want to write", list.get(0).getName());
    }

    // @Test
    // void get_shouldReturnById() {
    //     Long id = workspaceRepository.findAll().get(0).getId();
    //     WorkspaceResponse resp = workspaceService.get(id);
    //     assertNotNull(resp);
    //     assertEquals(id, resp.getId());
    // }

    @Test
    void create_shouldWork() {
        WorkspaceCreateRequest req = new WorkspaceCreateRequest();
        req.setName("Second WS");
        req.setDescription("Another description");

        WorkspaceResponse created = workspaceService.create(req);
        assertNotNull(created.getId());
        assertEquals("Second WS", created.getName());
    }

    @Test
    void create_shouldFailOnDuplicateName() {
        WorkspaceCreateRequest dup = new WorkspaceCreateRequest();
        dup.setName("Anything you want to write");

        assertThrows(ResponseStatusException.class, () -> workspaceService.create(dup));
    }

    // @Test
    // void patch_shouldUpdateDescription() {
    //     Long id = workspaceRepository.findAll().get(0).getId();

    //     WorkspaceUpdateRequest up = new WorkspaceUpdateRequest();
    //     up.setDescription("New desc");

    //     WorkspaceResponse updated = workspaceService.patch(id, up);
    //     assertEquals("New desc", updated.getDescription());
    // }

    // @Test
    // void delete_shouldRemove() {
    //     Long id = workspaceRepository.findAll().get(0).getId();
    //     workspaceService.delete(id);
    //     assertTrue(workspaceRepository.findById(id).isEmpty());
    // }

    // @Test
    // void delete_shouldThrowNotFound() {
    //     assertThrows(ResponseStatusException.class, () -> workspaceService.delete(999L));
    // }
}
