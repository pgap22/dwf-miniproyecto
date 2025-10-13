package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sv.edu.udb.data_collector.controller.response.RecordResponse;
import sv.edu.udb.data_collector.controller.response.RecordValueResponse;
import sv.edu.udb.data_collector.domain.RecordEntity;
import sv.edu.udb.data_collector.domain.RecordValue;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface RecordMapper {

    @Mapping(source = "schema.id", target = "schemaId")
    @Mapping(source = "values", target = "values")
    RecordResponse toResponse(RecordEntity record);

    @Mapping(source = "attribute.id",   target = "attributeId")
    @Mapping(source = "attribute.name", target = "attributeName")
    @Mapping(source = "catalogItem.id", target = "catalogItemId")
    RecordValueResponse toResponse(RecordValue value);
}
