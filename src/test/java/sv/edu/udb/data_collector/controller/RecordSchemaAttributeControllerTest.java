package sv.edu.udb.data_collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeUpdateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.RecordSchemaAttributeService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RecordSchemaAttributeController.class,
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
class RecordSchemaAttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordSchemaAttributeService attributeService;

    @Autowired
    private ObjectMapper objectMapper;

    private RecordSchemaAttributeResponse attributeResponse;

    @BeforeEach
    void setUp() {
        attributeResponse = RecordSchemaAttributeResponse.builder()
                .id("attr-1")
                .name("Test Attribute")
                .recordSchemaId("schema-1")
                .isRequired(true)
                .allowMultiple(false)
                .dataTypeId("dt-1")
                .build();
    }

    @Nested
    @DisplayName("Pruebas de endpoints de Atributos")
    class AttributeEndpointsTests {
        @Test
        @DisplayName("POST /record-schemas/{id}/attributes - Debe crear un atributo y devolver 201 Created")
        void addAttributeToSchema_shouldReturnCreated() throws Exception {
            // Arrange
            RecordSchemaAttributeCreateRequest request = new RecordSchemaAttributeCreateRequest();
            request.setName("Test Attribute");
            request.setDataTypeId("dt-1");
            request.setIsRequired(true);
            request.setAllowMultiple(false);

            given(attributeService.add(any(String.class), any(RecordSchemaAttributeCreateRequest.class)))
                    .willReturn(attributeResponse);

            // Act & Assert
            mockMvc.perform(post("/api/record-schemas/{schemaId}/attributes", "schema-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is("attr-1")))
                    .andExpect(jsonPath("$.name", is("Test Attribute")));
        }

        @Test
        @DisplayName("GET /attributes/{id} - Debe devolver un atributo por ID y 200 OK")
        void getAttributeById_whenFound_shouldReturnOk() throws Exception {
            // Arrange
            given(attributeService.findById(anyString())).willReturn(attributeResponse);

            // Act & Assert
            mockMvc.perform(get("/api/attributes/{attributeId}", "attr-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("attr-1")));
        }

        @Test
        @DisplayName("GET /attributes/{id} - Debe devolver 404 Not Found si el atributo no existe")
        void getAttributeById_whenNotFound_shouldReturnNotFound() throws Exception {
            // Arrange
            given(attributeService.findById(anyString()))
                    .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get("/api/attributes/{attributeId}", "attr-inexistente"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /record-schemas/{schemaId}/attributes - Debe devolver una lista de atributos y 200 OK")
        void getAttributesBySchema_shouldReturnOk() throws Exception {
            // Arrange
            List<RecordSchemaAttributeResponse> attributes = Collections.singletonList(attributeResponse);
            given(attributeService.findBySchemaId(anyString())).willReturn(attributes);

            // Act & Assert
            mockMvc.perform(get("/api/record-schemas/{schemaId}/attributes", "schema-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is("attr-1")));
        }

        @Test
        @DisplayName("PATCH /attributes/{id} - Debe actualizar un atributo y devolver 200 OK")
        void updateAttribute_shouldReturnOk() throws Exception {
            // Arrange
            RecordSchemaAttributeUpdateRequest request = new RecordSchemaAttributeUpdateRequest();
            request.setName("Updated Name");

            RecordSchemaAttributeResponse updatedResponse = RecordSchemaAttributeResponse.builder()
                    .id("attr-1")
                    .name("Updated Name")
                    .recordSchemaId("schema-1")
                    .build();

            given(attributeService.update(anyString(), any(RecordSchemaAttributeUpdateRequest.class)))
                    .willReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(patch("/api/attributes/{attributeId}", "attr-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("attr-1")))
                    .andExpect(jsonPath("$.name", is("Updated Name")));
        }

        @Test
        @DisplayName("DELETE /attributes/{id} - Debe eliminar un atributo y devolver 204 No Content")
        void removeAttribute_shouldReturnNoContent() throws Exception {
            // Arrange
            doNothing().when(attributeService).remove(anyString());

            // Act & Assert
            mockMvc.perform(delete("/api/attributes/{attributeId}", "attr-1"))
                    .andExpect(status().isNoContent());

            verify(attributeService, times(1)).remove("attr-1");
        }
    }
}