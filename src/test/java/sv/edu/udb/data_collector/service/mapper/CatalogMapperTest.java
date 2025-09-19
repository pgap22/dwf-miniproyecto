package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.Workspace;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogMapperTest {

    private final CatalogMapper mapper = Mappers.getMapper(CatalogMapper.class);

    @Test
    @DisplayName("Debe mapear una entidad Catalog a un CatalogResponse correctamente")
    void shouldMapCatalogToResponse() {
        // Arrange
        Workspace workspace = Workspace.builder().id("ws-1").build();
        Catalog entity = Catalog.builder()
                .id("cat-1")
                .name("Países")
                .description("Lista de países")
                .workspace(workspace)
                .build();

        // Act
        CatalogResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("cat-1");
        assertThat(response.getName()).isEqualTo("Países");
        assertThat(response.getDescription()).isEqualTo("Lista de países");
        assertThat(response.getWorkspaceId()).isEqualTo("ws-1");
    }

    @Test
    @DisplayName("Debe devolver nulo si la entidad Catalog de entrada es nula")
    void shouldReturnNullWhenCatalogIsNull() {
        // Act
        CatalogResponse response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}