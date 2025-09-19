package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.CatalogItemRepository;
import sv.edu.udb.data_collector.repository.CatalogRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.implementation.CatalogServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private CatalogRepository catalogRepository;
    @Mock
    private CatalogItemRepository itemRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;

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
        @DisplayName("Debe crear un catálogo exitosamente")
        void createCatalog_whenDataIsValid_shouldSucceed() {
            // Arrange
            given(workspaceRepository.findById("ws-1")).willReturn(Optional.of(workspace));
            given(catalogRepository.existsByNameAndWorkspaceId("Nuevo Catálogo", "ws-1")).willReturn(false);
            given(catalogRepository.save(any(Catalog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            Catalog result = catalogService.createCatalog("ws-1", "Nuevo Catálogo", "Descripción");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Nuevo Catálogo");
            assertThat(result.getWorkspace().getId()).isEqualTo("ws-1");
            verify(catalogRepository).save(any(Catalog.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si el nombre del catálogo ya existe en el workspace")
        void createCatalog_whenNameExists_shouldThrowException() {
            // Arrange
            given(workspaceRepository.findById("ws-1")).willReturn(Optional.of(workspace));
            given(catalogRepository.existsByNameAndWorkspaceId("Nombre Repetido", "ws-1")).willReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class, () -> {
                catalogService.createCatalog("ws-1", "Nombre Repetido", "Desc");
            });
        }
    }

    // --- Pruebas para la gestión de Ítems de Catálogo ---
    @Nested
    @DisplayName("Pruebas de Ítems de Catálogo")
    class CatalogItemTests {
        @Test
        @DisplayName("Debe crear un ítem de catálogo exitosamente")
        void createItem_whenDataIsValid_shouldSucceed() {
            // Arrange
            given(catalogRepository.findById("cat-1")).willReturn(Optional.of(catalog));
            given(itemRepository.existsByCatalogIdAndValue("cat-1", "Nuevo Valor")).willReturn(false);
            given(itemRepository.save(any(CatalogItem.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            CatalogItem result = catalogService.createItem("cat-1", "Nuevo Valor");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo("Nuevo Valor");
            assertThat(result.getCatalog().getId()).isEqualTo("cat-1");
            verify(itemRepository).save(any(CatalogItem.class));
        }

        @Test
        @DisplayName("Debe actualizar un ítem de catálogo exitosamente")
        void updateItem_whenDataIsValid_shouldSucceed() {
            // Arrange
            given(itemRepository.findByIdAndCatalog_Id("item-1", "cat-1")).willReturn(Optional.of(catalogItem));
            given(itemRepository.existsByCatalogIdAndValue("cat-1", "Valor Actualizado")).willReturn(false);
            given(itemRepository.save(any(CatalogItem.class))).willReturn(catalogItem);

            // Act
            CatalogItem result = catalogService.updateItem("cat-1", "item-1", "Valor Actualizado");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo("Valor Actualizado");
            verify(itemRepository).save(catalogItem);
        }

        @Test
        @DisplayName("Debe lanzar excepción al actualizar si el nuevo valor ya existe")
        void updateItem_whenValueExists_shouldThrowException() {
            // Arrange
            given(itemRepository.findByIdAndCatalog_Id("item-1", "cat-1")).willReturn(Optional.of(catalogItem));
            given(itemRepository.existsByCatalogIdAndValue("cat-1", "Valor Duplicado")).willReturn(true);

            // Act & Assert
            assertThrows(ResponseStatusException.class, () -> {
                catalogService.updateItem("cat-1", "item-1", "Valor Duplicado");
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
    }
}