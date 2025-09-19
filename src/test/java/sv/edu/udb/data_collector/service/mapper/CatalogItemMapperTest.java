package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.domain.Catalog;
import sv.edu.udb.data_collector.domain.CatalogItem;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogItemMapperTest {

    private final CatalogItemMapper mapper = Mappers.getMapper(CatalogItemMapper.class);

    @Test
    @DisplayName("Debe mapear una entidad CatalogItem a un CatalogItemResponse correctamente")
    void shouldMapCatalogItemToResponse() {
        // Arrange
        Catalog catalog = Catalog.builder().id("cat-1").build();
        CatalogItem entity = CatalogItem.builder()
                .id("item-1")
                .value("El Salvador")
                .catalog(catalog)
                .build();

        // Act
        CatalogItemResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("item-1");
        assertThat(response.getValue()).isEqualTo("El Salvador");
        assertThat(response.getCatalogId()).isEqualTo("cat-1");
    }

    @Test
    @DisplayName("Debe devolver nulo si la entidad CatalogItem de entrada es nula")
    void shouldReturnNullWhenCatalogItemIsNull() {
        // Act
        CatalogItemResponse response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}