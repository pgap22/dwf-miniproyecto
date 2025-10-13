package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.domain.RecordEntity;

public interface RecordService {
    RecordEntity create(String schemaId, CreateRecordRequest request, String currentUsername); // schemaId ahora es String
}
