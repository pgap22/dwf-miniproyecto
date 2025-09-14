package sv.edu.udb.data_collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad en los tests
@ActiveProfiles("test")
class WorkspaceControllerIT {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WorkspaceRepository repository;

    @BeforeEach
    void setUp() {
        // Arrange (común): BD limpia para cada prueba
        repository.deleteAll();
    }

    @Test
    void create_debeRetornar201_conLocationYBody() throws Exception {
        // Arrange
        var requestBody = Map.of("name", "Alpha");

        // Act + Assert
        mvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/workspaces/")))
                .andExpect(jsonPath("$.id", not(isEmptyString())))
                .andExpect(jsonPath("$.name").value("Alpha"))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void create_conNombreDuplicado_debeRetornar400() throws Exception {
        // Arrange
        repository.save(Workspace.builder().name("Alpha").build());
        var requestBody = Map.of("name", "alpha"); // case-insensitive

        // Act + Assert
        mvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_debeRetornar200_conEntidad() throws Exception {
        // Arrange
        var saved = repository.save(Workspace.builder().name("Dev").build());

        // Act + Assert
        mvc.perform(get("/api/workspaces/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Dev"));
    }

    @Test
    void list_debeRetornar200_conElementos() throws Exception {
        // Arrange
        repository.save(Workspace.builder().name("A").build());
        repository.save(Workspace.builder().name("B").build());

        // Act + Assert
        mvc.perform(get("/api/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("A", "B")));
    }

    @Test
    void patch_debeActualizarNombre_yReflejarseEnGet() throws Exception {
        // Arrange
        var saved = repository.save(Workspace.builder().name("Old").build());
        var requestBody = Map.of("name", "New");

        // Act + Assert (PATCH)
        mvc.perform(patch("/api/workspaces/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));

        // Act + Assert (GET de verificación)
        mvc.perform(get("/api/workspaces/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void delete_debeRetornar204_yLuegoGet404() throws Exception {
        // Arrange
        var saved = repository.save(Workspace.builder().name("Temp").build());

        // Act + Assert (DELETE)
        mvc.perform(delete("/api/workspaces/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        // Act + Assert (GET confirma que ya no existe)
        mvc.perform(get("/api/workspaces/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void notFound_debeRetornar404_enGetPatchDelete() throws Exception {
        // Arrange
        var inexistente = "nope";

        // Act + Assert
        mvc.perform(get("/api/workspaces/{id}", inexistente))
                .andExpect(status().isNotFound());

        mvc.perform(patch("/api/workspaces/{id}", inexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "X"))))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/workspaces/{id}", inexistente))
                .andExpect(status().isNotFound());
    }
}
