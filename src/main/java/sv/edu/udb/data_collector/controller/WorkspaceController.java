package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.WorkspaceCreateRequest;
import sv.edu.udb.data_collector.controller.request.WorkspaceUpdateRequest;
import sv.edu.udb.data_collector.controller.response.WorkspaceResponse;
import sv.edu.udb.data_collector.service.WorkspaceService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService service;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(@Valid @RequestBody WorkspaceCreateRequest request) {
        WorkspaceResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/workspaces/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public List<WorkspaceResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public WorkspaceResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public WorkspaceResponse patch(@PathVariable String id,
                                   @Valid @RequestBody WorkspaceUpdateRequest request) {
        return service.patch(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
