package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.domain.CatalogItem;

@Mapper(componentModel = "spring")
public interface CatalogItemMapper {
    @Mapping(target = "catalogId", source = "catalog.id")
    CatalogItemResponse toResponse(CatalogItem entity);
}
