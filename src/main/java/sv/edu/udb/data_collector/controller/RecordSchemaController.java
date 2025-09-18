package sv.edu.udb.data_collector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sv.edu.udb.data_collector.controller.request.CreateRecordSchemaRequest;
import sv.edu.udb.data_collector.controller.request.UpdateRecordSchemaRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.service.RecordSchemaService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaMapper;

import java.util.List;

@RestController
@RequestMapping("/api/record-schemas")
@RequiredArgsConstructor
public class RecordSchemaController {

    private final RecordSchemaService recordSchemeService;
    private final RecordSchemaMapper recordSchemeMapper;

    /**
     * Crea un nuevo RecordScheme.
     * Recibe un DTO, llama al servicio y devuelve otro DTO.
     */
    @PostMapping
    public ResponseEntity<RecordSchemaResponse> createScheme(@Valid @RequestBody CreateRecordSchemaRequest request) {
        // 1. Llama al servicio con los datos de la petición
        RecordSchema createdSchemeEntity = recordSchemeService.create(
            request.getWorkspaceId(),
            request.getName(),
            request.getDescription()
        );
        // 2. Mapea la entidad resultante a un DTO de respuesta y la devuelve
        return new ResponseEntity<>(recordSchemeMapper.toResponseDTO(createdSchemeEntity), HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los RecordSchemes de un Workspace específico.
     */
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<RecordSchemaResponse>> getSchemesByWorkspace(@PathVariable String workspaceId) {
        // 1. Obtiene la lista de entidades del servicio
        List<RecordSchema> schemeEntities = recordSchemeService.findAllByWorkspaceId(workspaceId);
        // 2. Mapea la lista completa a una lista de DTOs de respuesta
        return ResponseEntity.ok(recordSchemeMapper.toResponseDTOList(schemeEntities));
    }

    /**
     * Obtiene un RecordScheme por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecordSchemaResponse> getSchemeById(@PathVariable String id) {
        return recordSchemeService.findById(id)
                .map(recordSchemeMapper::toResponseDTO) // 3. Mapea la entidad a DTO si la encuentra
                .map(ResponseEntity::ok) // 4. Envuelve el DTO en una respuesta 200 OK
                .orElse(ResponseEntity.notFound().build()); // 5. Si no, devuelve 404
    }

    /**
     * Actualiza un RecordScheme existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecordSchemaResponse> updateScheme(@PathVariable String id, @Valid @RequestBody UpdateRecordSchemaRequest request) {
        // Crea un objeto RecordScheme temporal con los datos a actualizar
        RecordSchema dataToUpdate = RecordSchema.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        // 1. Llama al servicio para realizar la actualización
        RecordSchema updatedSchemeEntity = recordSchemeService.update(id, dataToUpdate);
        // 2. Mapea la entidad actualizada a un DTO de respuesta
        return ResponseEntity.ok(recordSchemeMapper.toResponseDTO(updatedSchemeEntity));
    }

    /**
     * Elimina un RecordScheme.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheme(@PathVariable String id) {
        recordSchemeService.delete(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }
}