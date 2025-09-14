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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    private long baseCount; // <-- conteo previo (por si hay datos sembrados por Flyway u otros tests)

    @BeforeEach
    void init() {
        baseCount = workspaceRepository.count(); // medir antes de sembrar

        Workspace ws = Workspace.builder()
                .name("Anything you want to write " + System.nanoTime()) // evitar colisiones por unique
                .build();
        workspaceRepository.save(ws);
    }

    @AfterEach
    void clean() {
        workspaceRepository.deleteAll();
    }

    @Test
    void shouldHasOneMoreWorkspace_When_FindAll_AfterSeed() {
        List<Workspace> list = workspaceRepository.findAll();
        assertNotNull(list);
        assertEquals(baseCount + 1, list.size()); // ya no asumimos “1” exacto
    }

    @Test
    void shouldSaveWorkspace_When_New() {
        Workspace ws = Workspace.builder()
                .name("Second WS " + System.nanoTime())
                .build();
        Workspace saved = workspaceRepository.save(ws);

        Workspace found = workspaceRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(saved.getName(), found.getName());
    }

    @Test
    void shouldDeleteWorkspace_When_Exists() {
        Workspace ws = Workspace.builder().name("To Delete " + System.nanoTime()).build();
        ws = workspaceRepository.save(ws);

        String id = ws.getId();
        assertTrue(workspaceRepository.findById(id).isPresent());

        workspaceRepository.deleteById(id);
        assertTrue(workspaceRepository.findById(id).isEmpty());
    }
}
