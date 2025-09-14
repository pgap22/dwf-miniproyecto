package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sv.edu.udb.data_collector.domain.Workspace;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository; // <— inyección por campo

    @BeforeEach
    void init() {
        Workspace ws = Workspace.builder()
                .name("Anything you want to write")
                .description("Initial description")
                .build();
        workspaceRepository.save(ws);
    }

    @AfterEach
    void clean() {
        workspaceRepository.deleteAll();
    }

    @Test
    void shouldHasOneWorkspace_When_FindAll() {
        List<Workspace> list = workspaceRepository.findAll();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void shouldGetWorkspace_When_IdExist() {
        Long id = workspaceRepository.findAll().get(0).getId(); // evita asumir 1L
        Workspace found = workspaceRepository.findById(id).orElse(null);
        assertNotNull(found);
        assertEquals("Anything you want to write", found.getName());
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }

    @Test
    void shouldSaveWorkspace_When_New() {
        Workspace ws = Workspace.builder()
                .name("Second WS")
                .description("Another description")
                .build();
        Workspace saved = workspaceRepository.save(ws);

        Workspace found = workspaceRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Second WS", found.getName());
        assertEquals("Another description", found.getDescription());
    }

    @Test
    void shouldDeleteWorkspace_When_Exists() {
        Workspace ws = Workspace.builder().name("To Delete").build();
        ws = workspaceRepository.save(ws);

        Long id = ws.getId();
        assertTrue(workspaceRepository.findById(id).isPresent());

        workspaceRepository.deleteById(id);
        assertTrue(workspaceRepository.findById(id).isEmpty());
    }
}
