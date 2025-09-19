package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.CreateAttributeRequest;
import sv.edu.udb.data_collector.controller.request.UpdateAttributeRequest;
import sv.edu.udb.data_collector.controller.response.AttributeResponse;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;
import sv.edu.udb.data_collector.service.RecordSchemaAttributeService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaAttributeMapper;

import java.util.List;

@RestController
@RequestMapping("/api") 
@RequiredArgsConstructor
public class RecordSchemaAttributeController {

    private final RecordSchemaAttributeService attributeService;
    private final RecordSchemaAttributeMapper attributeMapper;

    /**
     * Crea un nuevo atributo y lo asocia a un RecordSchema existente.
     */
    @PostMapping("/record-schemas/{schemaId}/attributes")
    public ResponseEntity<AttributeResponse> addAttributeToSchema(
            @PathVariable String schemaId,
            @Valid @RequestBody CreateAttributeRequest request) {
        
        RecordSchemaAttribute createdEntity = attributeService.addAttributeToSchema(schemaId, request);
        AttributeResponse response = attributeMapper.toResponse(createdEntity);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene todos los atributos de un RecordSchema específico.
     */
    @GetMapping("/record-schemas/{schemaId}/attributes")
    public ResponseEntity<List<AttributeResponse>> getAttributesBySchema(@PathVariable String schemaId) {
        List<RecordSchemaAttribute> attributeEntities = attributeService.findAttributesBySchemaId(schemaId);
        List<AttributeResponse> response = attributeMapper.toResponseList(attributeEntities);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene un atributo específico por su ID único.
     */
    @GetMapping("/attributes/{attributeId}")
    public ResponseEntity<AttributeResponse> getAttributeById(@PathVariable String attributeId) {
        return attributeService.findAttributeById(attributeId)
                .map(attributeMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un atributo existente.
     */
    @PatchMapping("/attributes/{attributeId}")
    public ResponseEntity<AttributeResponse> updateAttribute(
            @PathVariable String attributeId,
            @Valid @RequestBody UpdateAttributeRequest request) {

        RecordSchemaAttribute updatedEntity = attributeService.updateAttribute(attributeId, request);
        AttributeResponse response = attributeMapper.toResponse(updatedEntity);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un atributo por su ID.
     */
    @DeleteMapping("/attributes/{attributeId}")
    public ResponseEntity<Void> removeAttribute(@PathVariable String attributeId) {
        attributeService.removeAttribute(attributeId);
        return ResponseEntity.noContent().build();
    }
}