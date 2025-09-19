package sv.edu.udb.data_collector.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.domain.ValidationRule;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.ValidationRuleService;
import sv.edu.udb.data_collector.service.mapper.ValidationRuleMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
      controllers = ValidationRuleController.class,
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
class ValidationRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidationRuleService service;

    @MockBean
    private ValidationRuleMapper mapper;

    private ValidationRule ruleEntity;
    private ValidationRuleResponse ruleResponse;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        ruleEntity = ValidationRule.builder()
                .id(UUID.randomUUID().toString())
                .name("REQUIRED")
                .build();

        ruleResponse = ValidationRuleResponse.builder()
                .id(ruleEntity.getId())
                .name("REQUIRED")
                .build();
    }

    @Test
    @DisplayName("GET /api/validation-rules - Debe devolver una lista de reglas y 200 OK")
    void list_shouldReturnListOfRules() throws Exception {
        // Arrange
        List<ValidationRule> entityList = List.of(ruleEntity);
        List<ValidationRuleResponse> responseList = List.of(ruleResponse);

        given(service.findAll()).willReturn(entityList);
        given(mapper.toResponseList(entityList)).willReturn(responseList);

        // Act & Assert
        mockMvc.perform(get("/api/validation-rules"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("REQUIRED")));
    }

    @Test
    @DisplayName("GET /api/validation-rules/{id} - Debe devolver una regla por ID y 200 OK")
    void get_whenFound_shouldReturnRule() throws Exception {
        // Arrange
        given(service.findById(ruleEntity.getId())).willReturn(ruleEntity);
        given(mapper.toResponse(ruleEntity)).willReturn(ruleResponse);

        // Act & Assert
        mockMvc.perform(get("/api/validation-rules/{id}", ruleEntity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ruleEntity.getId())))
                .andExpect(jsonPath("$.name", is("REQUIRED")));
    }

    @Test
    @DisplayName("GET /api/validation-rules/{id} - Debe devolver 404 Not Found si la regla no existe")
    void get_whenNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        String nonExistentId = "id-que-no-existe";
        given(service.findById(nonExistentId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/validation-rules/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}