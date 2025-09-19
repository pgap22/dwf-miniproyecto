package sv.edu.udb.data_collector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.service.RecordSchemaService;

import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/record-schemas")
@RequiredArgsConstructor
public class RecordSchemaController {

    private final RecordSchemaService recordSchemaService;

    /**
     * Crea un nuevo RecordSchema.
     */
    @PostMapping
    public ResponseEntity<RecordSchemaResponse> createScheme(@Valid @RequestBody RecordSchemaRequestCreate request) {
        RecordSchemaResponse createdResponse = recordSchemaService.create(request);
        return ResponseEntity
                .created(URI.create("/api/record-schemas/" + createdResponse.getId()))
                .body(createdResponse);
    }

    /**
     * Obtiene todos los RecordSchemes de un Workspace específico.
     */
    @GetMapping("/workspace/{workspaceId}")
    public List<RecordSchemaResponse> getSchemesByWorkspace(@PathVariable String workspaceId) {
        return recordSchemaService.findAllByWorkspaceId(workspaceId);
    }

    /**
     * Obtiene un RecordSchema por su ID.
     */
    @GetMapping("/{id}")
    public RecordSchemaResponse getSchemeById(@PathVariable String id) {
        return recordSchemaService.findById(id);
    }

    /**
     * Actualiza un RecordSchema existente.
     */
    @PutMapping("/{id}")
    public RecordSchemaResponse updateScheme(@PathVariable String id, @Valid @RequestBody RecordSchemaRequestUpdate request) {
        return recordSchemaService.update(id, request);
    }

    /**
     * Elimina un RecordSchema.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheme(@PathVariable String id) {
        recordSchemaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}