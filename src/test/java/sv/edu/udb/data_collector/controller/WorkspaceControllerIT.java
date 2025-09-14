package sv.edu.udb.data_collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Levanta el contexto completo para probar el controlador con BD H2 (perfil test)
@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad si los hay
@ActiveProfiles("test")
class WorkspaceControllerIT {

    private final MockMvc mvc;
    private final ObjectMapper om;
    private final WorkspaceRepository workspaceRepository;

    WorkspaceControllerIT(MockMvc mvc, ObjectMapper om, WorkspaceRepository workspaceRepository) {
        this.mvc = mvc;
        this.om = om;
        this.workspaceRepository = workspaceRepository;
    }

    @BeforeEach
    void resetAndSeed() throws Exception {
        workspaceRepository.deleteAll();

        WorkspaceCreateRequest req = new WorkspaceCreateRequest();
        req.setName("Anything you want to write");
        req.setDescription("Initial description");

        mvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void list_shouldReturnOne() throws Exception {
        mvc.perform(get("/api/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Anything you want to write")));
    }

    @Test
    void get_shouldReturnById() throws Exception {
        Long id = workspaceRepository.findAll().get(0).getId();

        mvc.perform(get("/api/workspaces/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("Anything you want to write")));
    }

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        WorkspaceCreateRequest req = new WorkspaceCreateRequest();
        req.setName("Second WS");
        req.setDescription("Another description");

        mvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesRegex("/api/workspaces/\\d+")))
                .andExpect(jsonPath("$.name", is("Second WS")));
    }

    @Test
    void patch_shouldUpdateDescription() throws Exception {
        Long id = workspaceRepository.findAll().get(0).getId();

        String payload = """
                {"description":"New desc"}
                """;

        mvc.perform(patch("/api/workspaces/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.description", is("New desc")));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        Long id = workspaceRepository.findAll().get(0).getId();

        mvc.perform(delete("/api/workspaces/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/workspaces/{id}", id))
                .andExpect(status().isNotFound());
    }
}
