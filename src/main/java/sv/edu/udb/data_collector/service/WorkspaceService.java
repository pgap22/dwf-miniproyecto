package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
import sv.edu.udb.data_collector.controller.request.WorkspaceUpdateRequest;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {
    WorkspaceResponse create(WorkspaceCreateRequest request);
    WorkspaceResponse get(String id);
    List<WorkspaceResponse> list();
    WorkspaceResponse patch(String id, WorkspaceUpdateRequest request);
    void delete(String id);
}
