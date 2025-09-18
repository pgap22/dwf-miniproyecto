package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sv.edu.udb.data_collector.controller.request.UpdateAttributeRequest;
import sv.edu.udb.data_collector.controller.response.AttributeResponse;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecordSchemaAttributeMapper {

    /**
     * Mapea la entidad RecordSchemaAttribute a un objeto AttributeResponse.
     * Extrae los IDs de las entidades relacionadas para la respuesta de la API.
     */
    @Mapping(source = "recordSchema.id", target = "recordSchemaId")
    @Mapping(source = "dataType.id", target = "dataTypeId")
    @Mapping(source = "catalog.id", target = "catalogId") // MapStruct maneja si 'catalog' es nulo
    @Mapping(source = "dataType",  target = "datatype")
    AttributeResponse toResponse(RecordSchemaAttribute entity);

    /**
     * Mapea una lista de entidades a una lista de objetos de respuesta.
     */
    List<AttributeResponse> toResponseList(List<RecordSchemaAttribute> entities);

    @Mapping(source = "isRequired", target = "required") // Regla para isRequired
    @Mapping(source = "allowMultiple", target = "allowMultiple") // Regla para allowMultiple
    void updateFromRequest(UpdateAttributeRequest request, @MappingTarget RecordSchemaAttribute entity);
}