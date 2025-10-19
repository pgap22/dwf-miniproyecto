package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeCreateRequest;
import sv.edu.udb.data_collector.controller.request.RecordSchemaAttributeUpdateRequest;
import sv.edu.udb.data_collector.controller.response.RecordSchemaAttributeResponse;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.*;
import sv.edu.udb.data_collector.service.RecordSchemaAttributeService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaAttributeMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordSchemaAttributeServiceImpl implements RecordSchemaAttributeService {

    private final RecordSchemaAttributeRepository attributeRepository;
    private final RecordSchemaRepository schemaRepository;
    private final DataTypeRepository dataTypeRepository;
    private final CatalogRepository catalogRepository;
    private final RecordSchemaAttributeMapper attributeMapper;

    @Transactional
    public RecordSchemaAttributeResponse add(String recordSchemaId, RecordSchemaAttributeCreateRequest request) {
        RecordSchema schema = schemaRepository.findById(recordSchemaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RecordSchema no encontrado con id: " + recordSchemaId));

        attributeRepository.findByRecordSchemaIdAndName(recordSchemaId, request.getName())
                .ifPresent(attr -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El atributo '" + request.getName() + "' ya existe en este esquema.");
                });

        DataType dataType = dataTypeRepository.findById(request.getDataTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DataType no encontrado con id: " + request.getDataTypeId()));

        Catalog catalog = null;
        if (request.getCatalogId() != null) {
            catalog = catalogRepository.findById(request.getCatalogId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog no encontrado con id: " + request.getCatalogId()));
        }

        RecordSchemaAttribute newAttribute = attributeMapper.toRecordSchemaAttribute(request);
        newAttribute.setRecordSchema(schema);
        newAttribute.setDataType(dataType);
        newAttribute.setCatalog(catalog);

        RecordSchemaAttribute savedAttribute = attributeRepository.save(newAttribute);
        return attributeMapper.toResponse(savedAttribute);
    }

    @Transactional(readOnly = true)
    public List<RecordSchemaAttributeResponse> findBySchemaId(String recordSchemaId) {
        List<RecordSchemaAttribute> attributes = attributeRepository.findByRecordSchemaId(recordSchemaId);
        return attributeMapper.toResponseList(attributes);
    }

    @Transactional(readOnly = true)
    public RecordSchemaAttributeResponse findById(String attributeId) {
        RecordSchemaAttribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atributo no encontrado con id: " + attributeId));
        return attributeMapper.toResponse(attribute);
    }

    @Transactional
    public RecordSchemaAttributeResponse update(String attributeId, RecordSchemaAttributeUpdateRequest request) {
        RecordSchemaAttribute existingAttribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atributo no encontrado con id: " + attributeId));

        if (request.getDataTypeId() != null) {
            DataType newDataType = dataTypeRepository.findById(request.getDataTypeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DataType no encontrado con id: " + request.getDataTypeId()));
            existingAttribute.setDataType(newDataType);
        }
        if (request.getCatalogId() != null) {
            if (request.getCatalogId().isEmpty()) {
                existingAttribute.setCatalog(null);
            } else {
                Catalog newCatalog = catalogRepository.findById(request.getCatalogId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog no encontrado con id: " + request.getCatalogId()));
                existingAttribute.setCatalog(newCatalog);
            }
        }
        
        if (request.getName() != null && !request.getName().equals(existingAttribute.getName())) {
             attributeRepository.findByRecordSchemaIdAndName(existingAttribute.getRecordSchema().getId(), request.getName())
                .ifPresent(attr -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre del atributo '" + request.getName() + "' ya está en uso en este esquema.");
                });
        }
        
        attributeMapper.updateFromRequest(request, existingAttribute);

        RecordSchemaAttribute savedAttribute = attributeRepository.save(existingAttribute);
        return attributeMapper.toResponse(savedAttribute);
    }

    @Transactional
    public void remove(String attributeId) {
        attributeRepository.deleteById(attributeId);
    }
}