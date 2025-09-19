package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import sv.edu.udb.data_collector.controller.request.CatalogCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogUpdateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;

@Mapper(componentModel = "spring")
public interface CatalogMapper {
    @Mapping(target = "workspaceId", source = "workspace.id")

    CatalogResponse toResponse(Catalog entity);

    Catalog toCatalog(CatalogCreateRequest request);

    void updateCatalog(CatalogUpdateRequest request, @MappingTarget Catalog entity);
}
