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
import sv.edu.udb.data_collector.domain.Workspace;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CatalogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CatalogRepository catalogRepository;

    private Workspace workspace;
    private Catalog catalog1;

    @BeforeEach
    void setUp() {
        // Arrange
        workspace = Workspace.builder().name("Mi Workspace").build();
        entityManager.persist(workspace);

        catalog1 = Catalog.builder().name("Países").workspace(workspace).build();
        Catalog catalog2 = Catalog.builder().name("Monedas").workspace(workspace).build();
        entityManager.persist(catalog1);
        entityManager.persist(catalog2);
        entityManager.flush();
    }

    @Test
    @DisplayName("Debe encontrar todos los catálogos de un workspace, ordenados por nombre")
    void findAllByWorkspaceIdOrderByNameAsc_shouldReturnOrderedCatalogs() {
        // Act
        List<Catalog> catalogs = catalogRepository.findAllByWorkspaceIdOrderByNameAsc(workspace.getId());

        // Assert
        assertThat(catalogs).hasSize(2);
        assertThat(catalogs).extracting(Catalog::getName).containsExactly("Monedas", "Países");
    }

    @Test
    @DisplayName("Debe encontrar un catálogo por su ID y el ID de su workspace")
    void findByIdAndWorkspaceId_whenExists_shouldReturnCatalog() {
        // Act
        Optional<Catalog> foundCatalog = catalogRepository.findByIdAndWorkspaceId(catalog1.getId(), workspace.getId());
        Optional<Catalog> notFoundCatalog = catalogRepository.findByIdAndWorkspaceId(catalog1.getId(), "wrong-workspace-id");

        // Assert
        assertThat(foundCatalog).isPresent();
        assertThat(foundCatalog.get().getName()).isEqualTo("Países");
        assertThat(notFoundCatalog).isNotPresent();
    }

    @Test
    @DisplayName("Debe verificar si un catálogo existe por su nombre y workspace")
    void existsByNameAndWorkspaceId_shouldReturnCorrectBoolean() {
        // Act
        boolean shouldBeTrue = catalogRepository.existsByNameAndWorkspaceId("Países", workspace.getId());
        boolean shouldBeFalse = catalogRepository.existsByNameAndWorkspaceId("Ciudades", workspace.getId());

        // Assert
        assertThat(shouldBeTrue).isTrue();
        assertThat(shouldBeFalse).isFalse();
    }
}