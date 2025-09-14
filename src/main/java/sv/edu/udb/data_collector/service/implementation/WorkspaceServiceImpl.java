package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
import sv.edu.udb.data_collector.controller.request.WorkspaceUpdateRequest;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.WorkspaceService;
import sv.edu.udb.data_collector.service.mapper.WorkspaceMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository repository;
    private final WorkspaceMapper mapper;

    @Override
    public WorkspaceResponse create(WorkspaceCreateRequest request) {
        if (repository.existsByNameIgnoreCase(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workspace name already exists");
        }
        Workspace ws = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        ws = repository.save(ws);
        return mapper.toResponse(ws);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse get(Long id) {
        Workspace ws = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));
        return mapper.toResponse(ws);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public WorkspaceResponse patch(Long id, WorkspaceUpdateRequest request) {
        Workspace ws = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!request.getName().equalsIgnoreCase(ws.getName())
                    && repository.existsByNameIgnoreCase(request.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workspace name already exists");
            }
            ws.setName(request.getName());
        }
        if (request.getDescription() != null) {
            ws.setDescription(request.getDescription());
        }
        // updatedAt se actualiza con @PreUpdate
        return mapper.toResponse(ws);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found");
        }
        repository.deleteById(id);
    }
}
