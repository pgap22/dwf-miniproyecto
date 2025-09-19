package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;
import sv.edu.udb.data_collector.domain.Workspace;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CatalogItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private CatalogItemRepository catalogItemRepository;

    private Catalog catalog1;
    private Catalog catalog2;
    private CatalogItem item1a;

    @BeforeEach
    void setUp() {
        // Arrange
        Workspace workspace = Workspace.builder().name("Mi Workspace").build();
        entityManager.persist(workspace);

        catalog1 = Catalog.builder().name("Países").workspace(workspace).build();
        catalog2 = Catalog.builder().name("Monedas").workspace(workspace).build();
        entityManager.persist(catalog1);
        entityManager.persist(catalog2);

        item1a = CatalogItem.builder().value("Guatemala").catalog(catalog1).build();
        CatalogItem item1b = CatalogItem.builder().value("El Salvador").catalog(catalog1).build();
        CatalogItem item1c = CatalogItem.builder().value("Honduras").catalog(catalog1).build();
        CatalogItem item2a = CatalogItem.builder().value("USD").catalog(catalog2).build();

        entityManager.persist(item1a);
        entityManager.persist(item1b);
        entityManager.persist(item1c);
        entityManager.persist(item2a);
        entityManager.flush();
    }

    @Test
    @DisplayName("Debe encontrar todos los ítems de un catálogo, ordenados por valor")
    void findAllByCatalog_IdOrderByValue_shouldReturnOrderedItems() {
        // Act
        List<CatalogItem> items = catalogItemRepository.findAllByCatalog_IdOrderByValue(catalog1.getId());

        // Assert
        assertThat(items).hasSize(3);
        assertThat(items).extracting(CatalogItem::getValue).containsExactly("El Salvador", "Guatemala", "Honduras");
    }

    @Test
    @DisplayName("Debe encontrar un ítem por su ID y el ID de su catálogo")
    void findByIdAndCatalog_Id_whenExists_shouldReturnItem() {
        // Act
        Optional<CatalogItem> foundItem = catalogItemRepository.findByIdAndCatalog_Id(item1a.getId(), catalog1.getId());
        Optional<CatalogItem> notFoundItem = catalogItemRepository.findByIdAndCatalog_Id(item1a.getId(), catalog2.getId());

        // Assert
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getValue()).isEqualTo("Guatemala");
        assertThat(notFoundItem).isNotPresent();
    }

    @Test
    @DisplayName("Debe verificar si un ítem existe por su valor y catálogo")
    void existsByCatalogIdAndValue_shouldReturnCorrectBoolean() {
        // Act
        boolean shouldBeTrue = catalogItemRepository.existsByCatalogIdAndValue(catalog1.getId(), "El Salvador");
        boolean shouldBeFalse_wrongValue = catalogItemRepository.existsByCatalogIdAndValue(catalog1.getId(), "México");
        boolean shouldBeFalse_wrongCatalog = catalogItemRepository.existsByCatalogIdAndValue(catalog2.getId(), "El Salvador");

        // Assert
        assertThat(shouldBeTrue).isTrue();
        assertThat(shouldBeFalse_wrongValue).isFalse();
        assertThat(shouldBeFalse_wrongCatalog).isFalse();
    }
}