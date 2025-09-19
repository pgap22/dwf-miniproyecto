package sv.edu.udb.data_collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.RecordSchemaService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = RecordSchemaController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class RecordSchemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordSchemaService recordSchemaService;

    @Autowired
    private ObjectMapper objectMapper;

    private RecordSchemaResponse responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new RecordSchemaResponse();
        responseDTO.setId("rs-1");
        responseDTO.setName("Test Scheme");
        responseDTO.setDescription("Test Description");
        responseDTO.setWorkspaceId("ws-1");
    }

    @Test
    @DisplayName("POST /api/record-schemas - Debe crear un esquema y devolver 201 Created")
    void createScheme_shouldReturnCreated() throws Exception {
        // Arrange
        RecordSchemaRequestCreate request = new RecordSchemaRequestCreate();
        request.setWorkspaceId("ws-1");
        request.setName("Test Scheme");
        request.setDescription("Test Description");

        given(recordSchemaService.create(any(RecordSchemaRequestCreate.class))).willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/record-schemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("rs-1")))
                .andExpect(jsonPath("$.name", is("Test Scheme")))
                .andExpect(header().string("Location", "/api/record-schemas/" + responseDTO.getId()));
    }

    @Test
    @DisplayName("GET /api/record-schemas/{id} - Debe devolver un esquema por ID y 200 OK")
    void getSchemeById_whenFound_shouldReturnOk() throws Exception {
        // Arrange
        given(recordSchemaService.findById(any(String.class))).willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/record-schemas/{id}", "rs-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("rs-1")));
    }

    @Test
    @DisplayName("GET /api/record-schemas/{id} - Debe devolver 404 Not Found si el esquema no existe")
    void getSchemeById_whenNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        given(recordSchemaService.findById(any(String.class))).willThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/record-schemas/{id}", "rs-nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/record-schemas/workspace/{workspaceId} - Debe devolver una lista de esquemas y 200 OK")
    void getSchemesByWorkspace_shouldReturnOk() throws Exception {
        // Arrange
        List<RecordSchemaResponse> dtos = Collections.singletonList(responseDTO);
        given(recordSchemaService.findAllByWorkspaceId(any(String.class))).willReturn(dtos);

        // Act & Assert
        mockMvc.perform(get("/api/record-schemas/workspace/{workspaceId}", "ws-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("rs-1")));
    }

    @Test
    @DisplayName("PUT /api/record-schemas/{id} - Debe actualizar un esquema y devolver 200 OK")
    void updateScheme_shouldReturnOk() throws Exception {
        // Arrange
        RecordSchemaRequestUpdate request = new RecordSchemaRequestUpdate();
        request.setName("Updated Name");
        request.setDescription("Updated Description");
        
        RecordSchemaResponse updatedResponseDTO = new RecordSchemaResponse();
        updatedResponseDTO.setId("rs-1");
        updatedResponseDTO.setName("Updated Name");

        given(recordSchemaService.update(any(String.class), any(RecordSchemaRequestUpdate.class))).willReturn(updatedResponseDTO);
        
        // Act & Assert
        mockMvc.perform(put("/api/record-schemas/{id}", "rs-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("rs-1")))
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    @DisplayName("DELETE /api/record-schemas/{id} - Debe eliminar un esquema y devolver 204 No Content")
    void deleteScheme_shouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(recordSchemaService).delete(any(String.class));
        
        // Act & Assert
        mockMvc.perform(delete("/api/record-schemas/{id}", "rs-1"))
                .andExpect(status().isNoContent());
    }
}