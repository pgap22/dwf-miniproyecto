package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import sv.edu.udb.data_collector.controller.request.UpdateRecordSchemaRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.domain.RecordSchema;

import java.util.List;

@Mapper(componentModel = "spring") // Le dice a MapStruct que genere un Spring Bean
public interface RecordSchemaMapper {


    @Mapping(source = "workspace.id", target = "workspaceId")
    RecordSchemaResponse toResponseDTO(RecordSchema entity);
    
  
    List<RecordSchemaResponse> toResponseDTOList(List<RecordSchema> schemes);


    void updateFromRequest(UpdateRecordSchemaRequest request, @MappingTarget RecordSchema entity);
}