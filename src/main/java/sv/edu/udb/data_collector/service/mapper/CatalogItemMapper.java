package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import sv.edu.udb.data_collector.controller.request.CatalogItemCreateRequest;
import sv.edu.udb.data_collector.controller.request.CatalogItemUpdateRequest;
import sv.edu.udb.data_collector.controller.response.CatalogItemResponse;
import sv.edu.udb.data_collector.domain.CatalogItem;

/**
 * Componente que se encarga de mapear entre las entidades de dominio y los DTOs de peticiones/respuestas.
 * Utiliza MapStruct para generar el código de mapeo en tiempo de compilación.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)


public interface CatalogItemMapper {

    @Mapping(target = "catalogId", source = "catalog.id")
    CatalogItemResponse toResponse(CatalogItem entity);


    CatalogItem toCatalogItem(CatalogItemCreateRequest request);


    void updateCatalogItem(CatalogItemUpdateRequest request, @MappingTarget CatalogItem entity);
}