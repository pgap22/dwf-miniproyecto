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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.CatalogService;
import sv.edu.udb.data_collector.service.mapper.CatalogItemMapper;
import sv.edu.udb.data_collector.service.mapper.CatalogMapper;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@WebMvcTest(
        controllers = CatalogController.class,
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
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService service;

    @MockBean
    private CatalogMapper catalogMapper;

    @MockBean
    private CatalogItemMapper itemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Catalog catalogEntity;
    private CatalogResponse catalogResponse;
    private CatalogItem catalogItemEntity;
    private CatalogItemResponse catalogItemResponse;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        Workspace workspace = Workspace.builder().id("ws-1").build();
        catalogEntity = Catalog.builder().id("cat-1").name("Países").workspace(workspace).build();
        catalogResponse = CatalogResponse.builder().id("cat-1").name("Países").workspaceId("ws-1").build();
        
        catalogItemEntity = CatalogItem.builder().id("item-1").value("El Salvador").catalog(catalogEntity).build();
        catalogItemResponse = CatalogItemResponse.builder().id("item-1").value("El Salvador").catalogId("cat-1").build();
    }

    //--- Pruebas para los endpoints de Catalog ---

    @Test
    @DisplayName("Catalog: POST /api/catalogs - Debe crear un catálogo y devolver 201 Created")
    void createCatalog_shouldReturnCreated() throws Exception {
        // Arrange
        CatalogCreateRequest request = new CatalogCreateRequest();
        request.setName("Países");
        request.setWorkspaceId("ws-1");

        given(service.createCatalog(any(), any(), any())).willReturn(catalogEntity);
        given(catalogMapper.toResponse(any(Catalog.class))).willReturn(catalogResponse);

        // Act & Assert
        mockMvc.perform(post("/api/catalogs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/catalogs/cat-1"))
                .andExpect(jsonPath("$.id", is("cat-1")))
                .andExpect(jsonPath("$.name", is("Países")));
    }
    
    //--- Pruebas para los endpoints de CatalogItem ---

    @Test
    @DisplayName("CatalogItem: POST /api/catalogs/{id}/items - Debe crear un ítem y devolver 201 Created")
    void createItem_shouldReturnCreated() throws Exception {
        // Arrange
        CatalogItemCreateRequest request = new CatalogItemCreateRequest();
        request.setValue("El Salvador");

        given(service.createItem(any(), any())).willReturn(catalogItemEntity);
        given(itemMapper.toResponse(any(CatalogItem.class))).willReturn(catalogItemResponse);

        // Act & Assert
        mockMvc.perform(post("/api/catalogs/{catalogId}/items", "cat-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/catalogs/cat-1/items/item-1"))
                .andExpect(jsonPath("$.id", is("item-1")));
    }

    @Test
    @DisplayName("CatalogItem: DELETE /api/catalogs/{id}/items/{id} - Debe eliminar un ítem y devolver 204 No Content")
    void deleteItem_shouldReturnNoContent() throws Exception {
        // Arrange
        // No se necesita `given` para métodos void

        // Act & Assert
        mockMvc.perform(delete("/api/catalogs/{catalogId}/items/{itemId}", "cat-1", "item-1"))
                .andExpect(status().isNoContent());
    }
}