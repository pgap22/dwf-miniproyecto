package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeUpdateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;

import java.util.List;

public interface RecordSchemaAttributeService {

    /**
     * Añade un nuevo atributo a un esquema existente.
     */
    RecordSchemaAttributeResponse add(String recordSchemaId, RecordSchemaAttributeCreateRequest request);

    /**
     * Obtiene todos los atributos de un esquema específico.
     */
    List<RecordSchemaAttributeResponse> findBySchemaId(String recordSchemaId);

    /**
     * Obtiene un atributo por su ID único.
     */
    RecordSchemaAttributeResponse findById(String attributeId);

    /**
     * Actualiza un atributo existente.
     */
    RecordSchemaAttributeResponse update(String attributeId, RecordSchemaAttributeUpdateRequest request);

    /**
     * Elimina un atributo de un esquema.
     */
    void remove(String attributeId);
}