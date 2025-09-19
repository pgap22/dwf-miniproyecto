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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogUpdateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemUpdateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;

import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.CatalogService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
@AutoConfigureMockMvc(addFilters = false)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService service;

    @Autowired
    private ObjectMapper objectMapper;

    private CatalogResponse catalogResponse;
    private CatalogItemResponse catalogItemResponse;

    @BeforeEach
    void setUp() {
        // Arrange (preparación común)
        catalogResponse = CatalogResponse.builder().id("cat-1").name("Países").workspaceId("ws-1").build();
        catalogItemResponse = CatalogItemResponse.builder().id("item-1").value("El Salvador").catalogId("cat-1").build();
    }

    //--- Pruebas para los endpoints de Catalog ---

    @Nested
    @DisplayName("Pruebas de endpoints de Catálogo")
    class CatalogEndpointsTests {

        @Test
        @DisplayName("POST /api/catalogs - Debe crear un catálogo y devolver 201 Created")
        void createCatalog_shouldReturnCreated() throws Exception {
            // Arrange
            CatalogCreateRequest request = new CatalogCreateRequest();
            request.setName("Países");
            request.setWorkspaceId("ws-1");
            request.setDescription("Lista de países");

            given(service.createCatalog(any(CatalogCreateRequest.class))).willReturn(catalogResponse);

            // Act & Assert
            mockMvc.perform(post("/api/catalogs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/catalogs/cat-1"))
                    .andExpect(jsonPath("$.id", is(catalogResponse.getId())))
                    .andExpect(jsonPath("$.name", is(catalogResponse.getName())));
        }

        @Test
        @DisplayName("PUT /api/catalogs/{catalogId} - Debe actualizar un catálogo y devolver 200 OK")
        void updateCatalog_shouldReturnOk() throws Exception {
            // Arrange
            CatalogUpdateRequest request = new CatalogUpdateRequest();
            request.setName("Países Actualizados");

            CatalogResponse updatedResponse = CatalogResponse.builder()
                    .id("cat-1")
                    .name("Países Actualizados")
                    .build();

            given(service.updateCatalog(any(String.class), any(CatalogUpdateRequest.class)))
                    .willReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/catalogs/{catalogId}", "cat-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("cat-1")))
                    .andExpect(jsonPath("$.name", is("Países Actualizados")));
        }

        @Test
        @DisplayName("DELETE /api/catalogs/{catalogId} - Debe eliminar un catálogo y devolver 204 No Content")
        void deleteCatalog_shouldReturnNoContent() throws Exception {
            // Arrange
            doNothing().when(service).deleteCatalog(any(String.class));

            // Act & Assert
            mockMvc.perform(delete("/api/catalogs/{catalogId}", "cat-1"))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).deleteCatalog("cat-1");
        }

        @Test
        @DisplayName("GET /api/catalogs/{catalogId} - Debe obtener un catálogo y devolver 200 OK")
        void getCatalog_shouldReturnOk() throws Exception {
            // Arrange
            given(service.getCatalog(any(String.class))).willReturn(catalogResponse);

            // Act & Assert
            mockMvc.perform(get("/api/catalogs/{catalogId}", "cat-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(catalogResponse.getId())))
                    .andExpect(jsonPath("$.name", is(catalogResponse.getName())));
        }

        @Test
        @DisplayName("GET /api/catalogs - Debe listar catálogos y devolver 200 OK")
        void listCatalogs_shouldReturnOk() throws Exception {
            // Arrange
            List<CatalogResponse> catalogs = Collections.singletonList(catalogResponse);
            given(service.listCatalogs(any())).willReturn(catalogs);

            // Act & Assert
            mockMvc.perform(get("/api/catalogs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(catalogResponse.getId())));
        }
    }

    //--- Pruebas para los endpoints de CatalogItem ---
    @Nested
    @DisplayName("Pruebas de endpoints de Items de Catálogo")
    class CatalogItemEndpointsTests {

        @Test
        @DisplayName("POST /api/catalogs/{id}/items - Debe crear un ítem y devolver 201 Created")
        void createItem_shouldReturnCreated() throws Exception {
            // Arrange
            CatalogItemCreateRequest request = new CatalogItemCreateRequest();
            request.setValue("El Salvador");

            given(service.createItem(any(String.class), any(CatalogItemCreateRequest.class)))
                    .willReturn(catalogItemResponse);

            // Act & Assert
            mockMvc.perform(post("/api/catalogs/{catalogId}/items", "cat-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/catalogs/cat-1/items/item-1"))
                    .andExpect(jsonPath("$.id", is(catalogItemResponse.getId())));
        }

        @Test
        @DisplayName("PUT /api/catalogs/{id}/items/{id} - Debe actualizar un ítem y devolver 200 OK")
        void updateItem_shouldReturnOk() throws Exception {
            // Arrange
            CatalogItemUpdateRequest request = new CatalogItemUpdateRequest();
            request.setValue("Valor Actualizado");

            CatalogItemResponse updatedResponse = CatalogItemResponse.builder()
                    .id("item-1")
                    .value("Valor Actualizado")
                    .catalogId("cat-1")
                    .build();

            given(service.updateItem(any(String.class), any(String.class), any(CatalogItemUpdateRequest.class)))
                    .willReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/catalogs/{catalogId}/items/{itemId}", "cat-1", "item-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is("item-1")))
                    .andExpect(jsonPath("$.value", is("Valor Actualizado")));
        }

        @Test
        @DisplayName("DELETE /api/catalogs/{id}/items/{id} - Debe eliminar un ítem y devolver 204 No Content")
        void deleteItem_shouldReturnNoContent() throws Exception {
            // Arrange
            doNothing().when(service).deleteItem(any(String.class), any(String.class));

            // Act & Assert
            mockMvc.perform(delete("/api/catalogs/{catalogId}/items/{itemId}", "cat-1", "item-1"))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).deleteItem("cat-1", "item-1");
        }

        @Test
        @DisplayName("GET /api/catalogs/{id}/items/{id} - Debe obtener un ítem y devolver 200 OK")
        void getItem_shouldReturnOk() throws Exception {
            // Arrange
            given(service.getItem(any(String.class), any(String.class))).willReturn(catalogItemResponse);

            // Act & Assert
            mockMvc.perform(get("/api/catalogs/{catalogId}/items/{itemId}", "cat-1", "item-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(catalogItemResponse.getId())))
                    .andExpect(jsonPath("$.value", is(catalogItemResponse.getValue())));
        }

        @Test
        @DisplayName("GET /api/catalogs/{id}/items - Debe listar ítems y devolver 200 OK")
        void listItems_shouldReturnOk() throws Exception {
            // Arrange
            List<CatalogItemResponse> items = Collections.singletonList(catalogItemResponse);
            given(service.listItems(any(String.class))).willReturn(items);

            // Act & Assert
            mockMvc.perform(get("/api/catalogs/{catalogId}/items", "cat-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(catalogItemResponse.getId())));
        }
    }
}