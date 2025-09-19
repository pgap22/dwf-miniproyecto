package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemUpdateRequest;
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
    
    // --- Nuevo Test ---
    @Test
    @DisplayName("Debe mapear un CatalogItemCreateRequest a una entidad CatalogItem correctamente")
    void shouldMapCreateRequestToCatalogItem() {
        // Arrange
        CatalogItemCreateRequest createRequest = new CatalogItemCreateRequest();
        createRequest.setValue("New Item Value");

        // Act
        CatalogItem mappedEntity = mapper.toCatalogItem(createRequest);

        // Assert
        assertThat(mappedEntity).isNotNull();
        assertThat(mappedEntity.getValue()).isEqualTo("New Item Value");
        // Los otros campos deben ser nulos, ya que se asignan en el servicio
        assertThat(mappedEntity.getId()).isNull();
        assertThat(mappedEntity.getCatalog()).isNull();
    }
    
    // --- Nuevo Test ---
    @Test
    @DisplayName("Debe actualizar una entidad CatalogItem con los datos de un CatalogItemUpdateRequest")
    void shouldUpdateCatalogItemFromUpdateRequest() {
        // Arrange
        CatalogItemUpdateRequest updateRequest = new CatalogItemUpdateRequest();
        updateRequest.setValue("Updated Value");

        CatalogItem entityToUpdate = CatalogItem.builder()
                .id("item-1")
                .value("Original Value")
                .build();

        // Act
        mapper.updateCatalogItem(updateRequest, entityToUpdate);

        // Assert
        assertThat(entityToUpdate.getValue()).isEqualTo("Updated Value");
        // Verificamos que el ID no haya cambiado
        assertThat(entityToUpdate.getId()).isEqualTo("item-1");
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