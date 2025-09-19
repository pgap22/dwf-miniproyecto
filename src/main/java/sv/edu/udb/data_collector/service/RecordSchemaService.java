package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;

import java.util.List;

/**
 * Interfaz de servicio para gestionar la lógica de negocio de los RecordSchemes.
 * Esta versión trabaja con DTOs de petición y respuesta.
 */
public interface RecordSchemaService {

 
    RecordSchemaResponse create(RecordSchemaRequestCreate request);


    List<RecordSchemaResponse> findAllByWorkspaceId(String workspaceId);


    RecordSchemaResponse findById(String id);

    RecordSchemaResponse update(String id, RecordSchemaRequestUpdate request);


    void delete(String id);
}