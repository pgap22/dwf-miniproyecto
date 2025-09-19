package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeUpdateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;
import sv.edu.udb.data_collector.service.RecordSchemaAttributeService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecordSchemaAttributeController {

    private final RecordSchemaAttributeService attributeService;

    /**
     * Crea un nuevo atributo y lo asocia a un RecordSchema existente.
     */
    @PostMapping("/record-schemas/{schemaId}/attributes")
    public ResponseEntity<RecordSchemaAttributeResponse> addAttributeToSchema(
            @PathVariable String schemaId,
            @Valid @RequestBody RecordSchemaAttributeCreateRequest request) {

        RecordSchemaAttributeResponse response = attributeService.add(schemaId, request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los atributos de un RecordSchema específico.
     */
    @GetMapping("/record-schemas/{schemaId}/attributes")
    public List<RecordSchemaAttributeResponse> getAttributesBySchema(@PathVariable String schemaId) {
        return attributeService.findBySchemaId(schemaId);
    }

    /**
     * Obtiene un atributo específico por su ID único.
     */
    @GetMapping("/attributes/{attributeId}")
    public RecordSchemaAttributeResponse getAttributeById(@PathVariable String attributeId) {
        return attributeService.findById(attributeId);
    }

    /**
     * Actualiza un atributo existente.
     */
    @PatchMapping("/attributes/{attributeId}")
    public RecordSchemaAttributeResponse updateAttribute(
            @PathVariable String attributeId,
            @Valid @RequestBody RecordSchemaAttributeUpdateRequest request) {

        return attributeService.update(attributeId, request);
    }

    /**
     * Elimina un atributo por su ID.
     */
    @DeleteMapping("/attributes/{attributeId}")
    public ResponseEntity<Void> removeAttribute(@PathVariable String attributeId) {
        attributeService.remove(attributeId);
        return ResponseEntity.noContent().build();
    }
}