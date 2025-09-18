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
import sv.edu.udb.data_collector.controller.request.CreateAttributeRequest;
import sv.edu.udb.data_collector.controller.response.AttributeResponse;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.RecordSchemaAttributeService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaAttributeMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(
    controllers = RecordSchemaAttributeController.class,
    excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
      excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { SecurityConfig.class, JwtAuthenticationFilter.class }
        )
    )
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordSchemaAttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordSchemaAttributeService attributeService;

    @MockBean
    private RecordSchemaAttributeMapper attributeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private RecordSchemaAttribute attributeEntity;
    private AttributeResponse attributeResponse;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        attributeEntity = RecordSchemaAttribute.builder().id("attr-1").name("Test Attribute").build();

        attributeResponse = new AttributeResponse();
        attributeResponse.setId("attr-1");
        attributeResponse.setName("Test Attribute");
        attributeResponse.setRecordSchemaId("schema-1");
    }

    @Test
    @DisplayName("POST /record-schemas/{id}/attributes - Debe crear un atributo y devolver 201 Created")
    void addAttributeToSchema_shouldReturnCreated() throws Exception {
        // Arrange
        CreateAttributeRequest request = new CreateAttributeRequest();
        request.setName("Test Attribute");
        request.setDataTypeId("dt-1");
        request.setIsRequired(true);
        request.setAllowMultiple(false);
        
        given(attributeService.addAttributeToSchema(any(String.class), any(CreateAttributeRequest.class)))
                .willReturn(attributeEntity);
        given(attributeMapper.toResponse(any(RecordSchemaAttribute.class)))
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
        given(attributeService.findAttributeById("attr-1")).willReturn(Optional.of(attributeEntity));
        given(attributeMapper.toResponse(attributeEntity)).willReturn(attributeResponse);

        // Act & Assert
        mockMvc.perform(get("/api/attributes/{attributeId}", "attr-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("attr-1")));
    }

    @Test
    @DisplayName("GET /attributes/{id} - Debe devolver 404 Not Found si el atributo no existe")
    void getAttributeById_whenNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        given(attributeService.findAttributeById("attr-inexistente")).willReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/attributes/{attributeId}", "attr-inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /attributes/{id} - Debe eliminar un atributo y devolver 204 No Content")
    void removeAttribute_shouldReturnNoContent() throws Exception {
        // Arrange
        // No se necesita 'given' porque el método del servicio devuelve void.
        // Mockito por defecto no hace nada para métodos void, lo cual es perfecto.
        
        // Act & Assert
        mockMvc.perform(delete("/api/attributes/{attributeId}", "attr-1"))
                .andExpect(status().isNoContent());
    }
}