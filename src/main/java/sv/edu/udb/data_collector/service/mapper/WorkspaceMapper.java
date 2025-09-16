package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.domain.Workspace;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {

    // MapStruct generará la implementación automáticamente.
    WorkspaceResponse toResponse(Workspace ws);
}
