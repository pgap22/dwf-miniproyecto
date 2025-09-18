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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.request.CreateRecordSchemeRequest;
import sv.edu.udb.data_collector.controller.request.UpdateRecordSchemeRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemeResponse;
import sv.edu.udb.data_collector.domain.RecordScheme;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.RecordSchemeService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemeMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(
      controllers = RecordSchemeController.class,
      excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
      excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { SecurityConfig.class, JwtAuthenticationFilter.class }
        )
) // Carga solo el contexto web para este controlador
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordSchemeControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permite simular peticiones HTTP

    @MockBean // Crea un mock y lo añade al contexto de Spring
    private RecordSchemeService recordSchemeService;

    @MockBean
    private RecordSchemeMapper recordSchemeMapper;

    @Autowired
    private ObjectMapper objectMapper; // Utilidad para convertir objetos a JSON

    private RecordScheme recordScheme;
    private RecordSchemeResponse responseDTO;

    @BeforeEach
    void setUp() {
        Workspace workspace = Workspace.builder().id("ws-1").build();
        recordScheme = RecordScheme.builder()
                .id("rs-1")
                .name("Test Scheme")
                .description("Test Description")
                .workspace(workspace)
                .build();
        
        responseDTO = new RecordSchemeResponse();
        responseDTO.setId("rs-1");
        responseDTO.setName("Test Scheme");
        responseDTO.setDescription("Test Description");
        responseDTO.setWorkspaceId("ws-1");
    }

    @Test
    @DisplayName("POST /api/record-schemes - Debe crear un esquema y devolver 201 Created")
    void createScheme_shouldReturnCreated() throws Exception {
        // Arrange
        CreateRecordSchemeRequest request = new CreateRecordSchemeRequest();
        request.setWorkspaceId("ws-1");
        request.setName("Test Scheme");
        request.setDescription("Test Description");

        given(recordSchemeService.create(anyString(), anyString(), anyString())).willReturn(recordScheme);
        given(recordSchemeMapper.toResponseDTO(any(RecordScheme.class))).willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/record-schemes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("rs-1")))
                .andExpect(jsonPath("$.name", is("Test Scheme")));
    }

    @Test
    @DisplayName("GET /api/record-schemes/{id} - Debe devolver un esquema por ID y 200 OK")
    void getSchemeById_whenFound_shouldReturnOk() throws Exception {
        // Arrange
        given(recordSchemeService.findById("rs-1")).willReturn(Optional.of(recordScheme));
        given(recordSchemeMapper.toResponseDTO(recordScheme)).willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/record-schemes/{id}", "rs-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("rs-1")));
    }

    @Test
    @DisplayName("GET /api/record-schemes/{id} - Debe devolver 404 Not Found si el esquema no existe")
    void getSchemeById_whenNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        given(recordSchemeService.findById("rs-nonexistent")).willReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/record-schemes/{id}", "rs-nonexistent"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GET /api/record-schemes/workspace/{workspaceId} - Debe devolver una lista de esquemas y 200 OK")
    void getSchemesByWorkspace_shouldReturnOk() throws Exception {
        // Arrange
        List<RecordScheme> schemes = Collections.singletonList(recordScheme);
        List<RecordSchemeResponse> dtos = Collections.singletonList(responseDTO);

        given(recordSchemeService.findAllByWorkspaceId("ws-1")).willReturn(schemes);
        given(recordSchemeMapper.toResponseDTOList(schemes)).willReturn(dtos);

        // Act & Assert
        mockMvc.perform(get("/api/record-schemes/workspace/{workspaceId}", "ws-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("rs-1")));
    }

    @Test
    @DisplayName("PUT /api/record-schemes/{id} - Debe actualizar un esquema y devolver 200 OK")
    void updateScheme_shouldReturnOk() throws Exception {
        // Arrange
        UpdateRecordSchemeRequest request = new UpdateRecordSchemeRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Description");

        // Simulación
        given(recordSchemeService.update(anyString(), any(RecordScheme.class))).willReturn(recordScheme);
        given(recordSchemeMapper.toResponseDTO(any(RecordScheme.class))).willReturn(responseDTO);
        
        // Act & Assert
        mockMvc.perform(put("/api/record-schemes/{id}", "rs-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("rs-1")));
    }

    @Test
    @DisplayName("DELETE /api/record-schemes/{id} - Debe eliminar un esquema y devolver 204 No Content")
    void deleteScheme_shouldReturnNoContent() throws Exception {
        // Arrange
        // No necesitamos simular nada en el servicio porque el método delete devuelve void
        
        // Act & Assert
        mockMvc.perform(delete("/api/record-schemes/{id}", "rs-1"))
                .andExpect(status().isNoContent());
    }
}
