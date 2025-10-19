package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sv.edu.udb.data_collector.controller.response.RecordResponse;
import sv.edu.udb.data_collector.domain.RecordEntity;


@Mapper(componentModel = "spring")
public interface RecordMapper {

    @Mapping(source = "schema.id", target = "schemaId")
    @Mapping(source = "user.id", target = "userId")
    RecordResponse toResponse(RecordEntity record);
       
}
