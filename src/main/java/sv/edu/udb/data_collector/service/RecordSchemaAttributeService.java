package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.CreateAttributeRequest;
import sv.edu.udb.data_collector.controller.request.UpdateAttributeRequest;
import sv.edu.udb.data_collector.domain.RecordSchemaAttribute;

import java.util.List;
import java.util.Optional;

public interface RecordSchemaAttributeService {

    /**
     * Añade un nuevo atributo a un esquema existente.
     */
    RecordSchemaAttribute addAttributeToSchema(String recordSchemaId, CreateAttributeRequest request);

    /**
     * Obtiene todos los atributos de un esquema específico.
     */
    List<RecordSchemaAttribute> findAttributesBySchemaId(String recordSchemaId);

    /**
     * Obtiene un atributo por su ID único.
     */
    Optional<RecordSchemaAttribute> findAttributeById(String attributeId);

    /**
     * Actualiza un atributo existente.
     */
    RecordSchemaAttribute updateAttribute(String attributeId, UpdateAttributeRequest request);

    /**
     * Elimina un atributo de un esquema.
     */
    void removeAttribute(String attributeId);
}