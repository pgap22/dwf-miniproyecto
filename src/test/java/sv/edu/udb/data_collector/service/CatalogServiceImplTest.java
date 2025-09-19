package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemUpdateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.CatalogItemRepository;
import sv.edu.udb.data_collector.repository.CatalogRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.CatalogServiceImpl;
import sv.edu.udb.data_collector.service.mapper.CatalogItemMapper;
import sv.edu.udb.data_collector.service.mapper.CatalogMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private CatalogRepository catalogRepository;
    @Mock
    private CatalogItemRepository itemRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    
    // Usamos @Spy para los mappers porque queremos usar los métodos reales
    @Spy
    private CatalogMapper catalogMapper = Mappers.getMapper(CatalogMapper.class);
    @Spy
    private CatalogItemMapper itemMapper = Mappers.getMapper(CatalogItemMapper.class);

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Workspace workspace;
    private Catalog catalog;
    private CatalogItem catalogItem;

    @BeforeEach
    void setUp() {
        workspace = Workspace.builder().id("ws-1").name("Test Workspace").build();
        catalog = Catalog.builder().id("cat-1").name("Países").workspace(workspace).build();
        catalogItem = CatalogItem.builder().id("item-1").value("El Salvador").catalog(catalog).build();
    }

    // --- Pruebas para la gestión de Catálogos ---
    @Nested
    @DisplayName("Pruebas de Catálogos")
    class CatalogTests {
        @Test
        @DisplayName("Debe crear un catálogo exitosamente y devolver un DTO de respuesta")
        void createCatalog_whenDataIsValid_shouldReturnResponseDTO() {
            // Arrange
            CatalogCreateRequest createRequest = new CatalogCreateRequest();
            createRequest.setWorkspaceId("ws-1");
            createRequest.setName("Nuevo Catálogo");
            createRequest.setDescription("Descripción");

            given(workspaceRepository.findById("ws-1")).willReturn(Optional.of(workspace));
            given(catalogRepository.existsByNameAndWorkspaceId("Nuevo Catálogo", "ws-1")).willReturn(false);
            given(catalogRepository.save(any(Catalog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            CatalogResponse result = catalogService.createCatalog(createRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Nuevo Catálogo");
            assertThat(result.getWorkspaceId()).isEqualTo("ws-1");
            verify(catalogRepository).save(any(Catalog.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre del catálogo ya existe en el workspace")
        void createCatalog_whenNameExists_shouldThrowException() {
            // Arrange
            CatalogCreateRequest createRequest = new CatalogCreateRequest();
            createRequest.setWorkspaceId("ws-1");
            createRequest.setName("Nombre Repetido");

            given(workspaceRepository.findById("ws-1")).willReturn(Optional.of(workspace));
            given(catalogRepository.existsByNameAndWorkspaceId("Nombre Repetido", "ws-1")).willReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class, () -> {
                catalogService.createCatalog(createRequest);
            });
        }
        
        @Test
        @DisplayName("Debe obtener un catálogo y devolver un DTO de respuesta")
        void getCatalog_shouldReturnResponseDTO() {
            // Arrange
            given(catalogRepository.findById(catalog.getId())).willReturn(Optional.of(catalog));

            // Act
            CatalogResponse result = catalogService.getCatalog(catalog.getId());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(catalog.getId());
            assertThat(result.getName()).isEqualTo(catalog.getName());
            assertThat(result.getWorkspaceId()).isEqualTo(catalog.getWorkspace().getId());
        }
    }

    // --- Pruebas para la gestión de Ítems de Catálogo ---
    @Nested
    @DisplayName("Pruebas de Ítems de Catálogo")
    class CatalogItemTests {
        @Test
        @DisplayName("Debe crear un ítem de catálogo exitosamente y devolver un DTO de respuesta")
        void createItem_whenDataIsValid_shouldReturnResponseDTO() {
            // Arrange
            CatalogItemCreateRequest createRequest = new CatalogItemCreateRequest();
            createRequest.setValue("Nuevo Valor");

            given(catalogRepository.findById("cat-1")).willReturn(Optional.of(catalog));
            given(itemRepository.existsByCatalogIdAndValue("cat-1", "Nuevo Valor")).willReturn(false);
            given(itemRepository.save(any(CatalogItem.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            CatalogItemResponse result = catalogService.createItem("cat-1", createRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo("Nuevo Valor");
            assertThat(result.getCatalogId()).isEqualTo("cat-1");
            verify(itemRepository).save(any(CatalogItem.class));
        }

        @Test
        @DisplayName("Debe actualizar un ítem de catálogo exitosamente y devolver un DTO de respuesta")
        void updateItem_whenDataIsValid_shouldReturnResponseDTO() {
            // Arrange
            CatalogItemUpdateRequest updateRequest = new CatalogItemUpdateRequest();
            updateRequest.setValue("Valor Actualizado");

            given(itemRepository.findByIdAndCatalog_Id("item-1", "cat-1")).willReturn(Optional.of(catalogItem));
            given(itemRepository.existsByCatalogIdAndValue("cat-1", "Valor Actualizado")).willReturn(false);
            given(itemRepository.save(any(CatalogItem.class))).willReturn(catalogItem);

            // Act
            CatalogItemResponse result = catalogService.updateItem("cat-1", "item-1", updateRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo("Valor Actualizado");
            verify(itemRepository).save(catalogItem);
        }

        @Test
        @DisplayName("Debe lanzar excepción al actualizar si el nuevo valor ya existe")
        void updateItem_whenValueExists_shouldThrowException() {
            // Arrange
            CatalogItemUpdateRequest updateRequest = new CatalogItemUpdateRequest();
            updateRequest.setValue("Valor Duplicado");
            
            given(itemRepository.findByIdAndCatalog_Id("item-1", "cat-1")).willReturn(Optional.of(catalogItem));
            given(itemRepository.existsByCatalogIdAndValue("cat-1", "Valor Duplicado")).willReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class, () -> {
                catalogService.updateItem("cat-1", "item-1", updateRequest);
            });
        }

        @Test
        @DisplayName("Debe eliminar un ítem de catálogo")
        void deleteItem_shouldSucceed() {
            // Arrange
            given(itemRepository.findByIdAndCatalog_Id("item-1", "cat-1")).willReturn(Optional.of(catalogItem));
            doNothing().when(itemRepository).delete(catalogItem);

            // Act
            catalogService.deleteItem("cat-1", "item-1");

            // Assert
            verify(itemRepository, times(1)).delete(catalogItem);
        }
        
        @Test
        @DisplayName("Debe obtener un ítem de catálogo y devolver un DTO de respuesta")
        void getItem_shouldReturnResponseDTO() {
            // Arrange
            given(itemRepository.findByIdAndCatalog_Id(catalogItem.getId(), catalogItem.getCatalog().getId())).willReturn(Optional.of(catalogItem));
            
            // Act
            CatalogItemResponse result = catalogService.getItem(catalogItem.getCatalog().getId(), catalogItem.getId());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(catalogItem.getId());
            assertThat(result.getValue()).isEqualTo(catalogItem.getValue());
            assertThat(result.getCatalogId()).isEqualTo(catalogItem.getCatalog().getId());
        }
    }
}