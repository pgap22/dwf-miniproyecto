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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.response.DataTypeResponse;
import sv.edu.udb.data_collector.domain.DataType;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.DataTypeService;
import sv.edu.udb.data_collector.service.mapper.DataTypeMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DataTypeController.class,
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
class DataTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataTypeService service;

    @MockBean
    private DataTypeMapper mapper;

    private DataType stringEntity;
    private DataType numberEntity;
    private DataType catalogEntity;
    
    private DataTypeResponse stringResponse;
    private DataTypeResponse numberResponse;
    private DataTypeResponse catalogResponse;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        stringEntity = DataType.builder().id("dt-1").name("STRING").kind("string").build();
        numberEntity = DataType.builder().id("dt-2").name("NUMBER").kind("number").build();
        catalogEntity = DataType.builder().id("dt-3").name("CATALOG").kind("catalog").build();

        stringResponse = DataTypeResponse.builder().id("dt-1").name("STRING").kind("string").build();
        numberResponse = DataTypeResponse.builder().id("dt-2").name("NUMBER").kind("number").build();
        catalogResponse = DataTypeResponse.builder().id("dt-3").name("CATALOG").kind("catalog").build();
    }

    @Test
    @DisplayName("GET /api/data-types/primitives - Debe devolver los tipos primitivos y 200 OK")
    void listPrimitives_shouldReturnPrimitiveTypes() throws Exception {
        // Arrange
        given(service.listPrimitives()).willReturn(List.of(stringEntity, numberEntity));
        given(mapper.toResponse(stringEntity)).willReturn(stringResponse);
        given(mapper.toResponse(numberEntity)).willReturn(numberResponse);

        // Act & Assert
        mockMvc.perform(get("/api/data-types/primitives"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("STRING")))
                .andExpect(jsonPath("$[1].name", is("NUMBER")));
    }

    @Test
    @DisplayName("GET /api/data-types - Debe devolver todos los tipos y 200 OK")
    void listAll_shouldReturnAllTypes() throws Exception {
        // Arrange
        given(service.listAll()).willReturn(List.of(stringEntity, numberEntity, catalogEntity));
        given(mapper.toResponse(stringEntity)).willReturn(stringResponse);
        given(mapper.toResponse(numberEntity)).willReturn(numberResponse);
        given(mapper.toResponse(catalogEntity)).willReturn(catalogResponse);

        // Act & Assert
        mockMvc.perform(get("/api/data-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/data-types/{id} - Debe devolver un tipo por ID y 200 OK")
    void get_whenFound_shouldReturnDataType() throws Exception {
        // Arrange
        given(service.getById("dt-1")).willReturn(stringEntity);
        given(mapper.toResponse(stringEntity)).willReturn(stringResponse);

        // Act & Assert
        mockMvc.perform(get("/api/data-types/{id}", "dt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("dt-1")))
                .andExpect(jsonPath("$.name", is("STRING")));
    }

    @Test
    @DisplayName("GET /api/data-types/{id} - Debe devolver 404 Not Found si el tipo no existe")
    void get_whenNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        given(service.getById("dt-inexistente")).willReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/data-types/{id}", "dt-inexistente"))
                .andExpect(status().isNotFound());
    }
}