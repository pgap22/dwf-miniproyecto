package sv.edu.udb.data_collector.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.request.PatchRecordRequest;
import sv.edu.udb.data_collector.domain.RecordEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface RecordService {

    RecordEntity create(String schemaId, CreateRecordRequest request, String currentUsername);

    // --- GET lista (con filtros y paginación) ---
    Page<RecordEntity> list(
            String schemaId,
            String createdByEmail,
            OffsetDateTime createdFrom,
            OffsetDateTime createdTo,
            String stringAttrId,
            String stringContains,
            String numberAttrId,
            BigDecimal numberMin,
            BigDecimal numberMax,
            String boolAttrId,
            Boolean boolValue,
            Pageable pageable
    );

    // --- GET detalle ---
    RecordEntity getOne(String schemaId, Long recordId);

    // --- PATCH parcial con re-validación ---
    RecordEntity patch(String schemaId, Long recordId, PatchRecordRequest request, String currentUsername);

    // --- DELETE ---
    void delete(String schemaId, Long recordId);
}
