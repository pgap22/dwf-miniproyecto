package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeUpdateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecordSchemaAttributeMapper {

    @Mapping(source = "recordSchema.id", target = "recordSchemaId")
    @Mapping(source = "dataType.id", target = "dataTypeId")
    @Mapping(source = "catalog.id", target = "catalogId")
    RecordSchemaAttributeResponse toResponse(RecordSchemaAttribute entity);

    List<RecordSchemaAttributeResponse> toResponseList(List<RecordSchemaAttribute> entities);


    @Mapping(target = "recordSchema", ignore = true)
    @Mapping(target = "dataType", ignore = true)
    @Mapping(target = "catalog", ignore = true)
    RecordSchemaAttribute toRecordSchemaAttribute(RecordSchemaAttributeCreateRequest request);


    @Mapping(target = "recordSchema", ignore = true)
    @Mapping(target = "dataType", ignore = true)
    @Mapping(target = "catalog", ignore = true)
    void updateFromRequest(RecordSchemaAttributeUpdateRequest request, @MappingTarget RecordSchemaAttribute entity);
}