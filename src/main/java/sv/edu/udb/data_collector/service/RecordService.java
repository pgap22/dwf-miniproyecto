package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.response.RecordResponse;


public interface RecordService {

    RecordResponse create(CreateRecordRequest request);

    // // --- GET lista (con filtros y paginación) ---
    // List<RecordResponse> list(String schemaId);

    // // --- GET detalle ---
    // RecordResponse getOne(String schemaId, Long recordId);

    // // --- PATCH parcial con re-validación ---
    // RecordResponse patch(String schemaId, Long recordId, PatchRecordRequest request, String currentUsername);

    // // --- DELETE ---
    // void delete(String schemaId, Long recordId);
}
