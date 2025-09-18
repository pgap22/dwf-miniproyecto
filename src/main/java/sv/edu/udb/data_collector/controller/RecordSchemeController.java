package sv.edu.udb.data_collector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import sv.edu.udb.data_collector.controller.request.CreateRecordSchemeRequest;
import sv.edu.udb.data_collector.controller.request.UpdateRecordSchemeRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemeResponse;
import sv.edu.udb.data_collector.domain.RecordScheme;
import sv.edu.udb.data_collector.service.RecordSchemeService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemeMapper;

import java.util.List;

@RestController
@RequestMapping("/api/record-schemes")
@RequiredArgsConstructor
public class RecordSchemeController {

    private final RecordSchemeService recordSchemeService;
    private final RecordSchemeMapper recordSchemeMapper;

    /**
     * Crea un nuevo RecordScheme.
     * Recibe un DTO, llama al servicio y devuelve otro DTO.
     */
    @PostMapping
    public ResponseEntity<RecordSchemeResponse> createScheme(@Valid @RequestBody CreateRecordSchemeRequest request) {
        // 1. Llama al servicio con los datos de la petición
        RecordScheme createdSchemeEntity = recordSchemeService.create(
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
    public ResponseEntity<List<RecordSchemeResponse>> getSchemesByWorkspace(@PathVariable String workspaceId) {
        // 1. Obtiene la lista de entidades del servicio
        List<RecordScheme> schemeEntities = recordSchemeService.findAllByWorkspaceId(workspaceId);
        // 2. Mapea la lista completa a una lista de DTOs de respuesta
        return ResponseEntity.ok(recordSchemeMapper.toResponseDTOList(schemeEntities));
    }

    /**
     * Obtiene un RecordScheme por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecordSchemeResponse> getSchemeById(@PathVariable String id) {
        return recordSchemeService.findById(id)
                .map(recordSchemeMapper::toResponseDTO) // 3. Mapea la entidad a DTO si la encuentra
                .map(ResponseEntity::ok) // 4. Envuelve el DTO en una respuesta 200 OK
                .orElse(ResponseEntity.notFound().build()); // 5. Si no, devuelve 404
    }

    /**
     * Actualiza un RecordScheme existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecordSchemeResponse> updateScheme(@PathVariable String id, @Valid @RequestBody UpdateRecordSchemeRequest request) {
        // Crea un objeto RecordScheme temporal con los datos a actualizar
        RecordScheme dataToUpdate = RecordScheme.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        // 1. Llama al servicio para realizar la actualización
        RecordScheme updatedSchemeEntity = recordSchemeService.update(id, dataToUpdate);
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