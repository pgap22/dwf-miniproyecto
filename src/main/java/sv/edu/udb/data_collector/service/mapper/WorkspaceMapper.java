package sv.edu.udb.data_collector.service.mapper;

import org.springframework.stereotype.Component;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.domain.Workspace;

@Component
public class WorkspaceMapper {

    public WorkspaceResponse toResponse(Workspace ws) {
        if (ws == null) return null;
        return WorkspaceResponse.builder()
                .id(ws.getId())
                .name(ws.getName())
                .createdAt(ws.getCreatedAt())
                .updatedAt(ws.getUpdatedAt())
                .build();
    }
}
