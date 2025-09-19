package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogUpdateRequest;
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

    // --- Nuevos Tests ---

    @Test
    @DisplayName("Debe mapear un CatalogCreateRequest a una entidad Catalog correctamente")
    void shouldMapCreateRequestToCatalog() {
        // Arrange
        CatalogCreateRequest createRequest = new CatalogCreateRequest();
        createRequest.setName("Departamentos");
        createRequest.setDescription("Lista de departamentos de El Salvador");
        createRequest.setWorkspaceId("ws-2");

        // Act
        Catalog mappedEntity = mapper.toCatalog(createRequest);

        // Assert
        assertThat(mappedEntity).isNotNull();
        assertThat(mappedEntity.getName()).isEqualTo("Departamentos");
        assertThat(mappedEntity.getDescription()).isEqualTo("Lista de departamentos de El Salvador");
        // Los otros campos deben ser nulos, ya que se asignan en el servicio
        assertThat(mappedEntity.getId()).isNull();
        assertThat(mappedEntity.getWorkspace()).isNull();
    }

    @Test
    @DisplayName("Debe actualizar una entidad Catalog con los datos de un CatalogUpdateRequest")
    void shouldUpdateCatalogFromUpdateRequest() {
        // Arrange
        CatalogUpdateRequest updateRequest = new CatalogUpdateRequest();
        updateRequest.setName("Municipios");
        updateRequest.setDescription("Lista de municipios de El Salvador");

        Catalog entityToUpdate = Catalog.builder()
                .id("cat-3")
                .name("Zonas")
                .description("Zona de El Salvador")
                .build();

        // Act
        mapper.updateCatalog(updateRequest, entityToUpdate);

        // Assert
        assertThat(entityToUpdate.getName()).isEqualTo("Municipios");
        assertThat(entityToUpdate.getDescription()).isEqualTo("Lista de municipios de El Salvador");
        // Verificamos que el ID no haya cambiado
        assertThat(entityToUpdate.getId()).isEqualTo("cat-3");
    }
}