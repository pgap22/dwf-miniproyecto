package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.CreateRecordRequest;
import sv.edu.udb.data_collector.controller.request.CreateRecordValueRequest;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.*;
import sv.edu.udb.data_collector.service.RecordService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;
    private final RecordValueRepository recordValueRepository;

    private final RecordSchemaRepository recordSchemaRepository;
    private final RecordSchemaAttributeRepository recordSchemaAttributeRepository;
    private final CatalogItemRepository catalogItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RecordEntity create(String schemaId, CreateRecordRequest request, String currentUsername) {

        RecordSchema schema = recordSchemaRepository.findById(schemaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record schema not found"));

        User createdBy = null;
        if (currentUsername != null && !currentUsername.isBlank()) {
            createdBy = userRepository.findByEmail(currentUsername).orElse(null);
        }

        RecordEntity record = RecordEntity.builder()
                .schema(schema)
                .createdBy(createdBy)
                .build();

        record = recordRepository.save(record); // obtiene ID y createdAt

        List<RecordValue> values = new ArrayList<>();

        for (CreateRecordValueRequest it : request.getValues()) {
            RecordSchemaAttribute attribute = recordSchemaAttributeRepository.findById(it.getAttributeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute not found: " + it.getAttributeId()));

            // Validar que el atributo pertenece al schema (String vs String)
            if (!attribute.getRecordSchema().getId().equals(schemaId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attribute does not belong to schema");
            }

            CatalogItem catalogItem = null;
            if (it.getCatalogItemId() != null && !it.getCatalogItemId().isBlank()) {   // String
                catalogItem = catalogItemRepository.findById(it.getCatalogItemId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Catalog item not found: " + it.getCatalogItemId()));
            }

            RecordValue value = RecordValue.builder()
                    .record(record)
                    .attribute(attribute)
                    .stringValue(it.getStringValue())
                    .numberValue(it.getNumberValue())
                    .booleanValue(it.getBooleanValue())
                    .dateValue(it.getDateValue())
                    .catalogItem(catalogItem)
                    .build();

            values.add(value);
        }

        recordValueRepository.saveAll(values);
        record.setValues(values); // funciona con Lombok @Setter

        return record;
    }
}
