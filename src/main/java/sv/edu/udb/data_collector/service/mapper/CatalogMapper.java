package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sv.edu.udb.data_collector.controller.response.CatalogResponse;
import sv.edu.udb.data_collector.domain.Catalog;

@Mapper(componentModel = "spring")
public interface CatalogMapper {
    @Mapping(target = "workspaceId", source = "workspace.id")
    CatalogResponse toResponse(Catalog entity);
}
