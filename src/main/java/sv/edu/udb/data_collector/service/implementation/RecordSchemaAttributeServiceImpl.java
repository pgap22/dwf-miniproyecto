package sv.edu.udb.data_collector.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.data_collector.controller.request.CreateAttributeRequest;
import sv.edu.udb.data_collector.controller.request.UpdateAttributeRequest;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.*;
import sv.edu.udb.data_collector.service.RecordSchemaAttributeService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecordSchemaAttributeServiceImpl implements RecordSchemaAttributeService {

    private final RecordSchemaAttributeRepository attributeRepository;
    private final RecordSchemaRepository schemaRepository;
    private final DataTypeRepository dataTypeRepository;
    private final CatalogRepository catalogRepository;

    @Override
    @Transactional
    public RecordSchemaAttribute addAttributeToSchema(String recordSchemaId, CreateAttributeRequest request) {
        // 1. Validar que el RecordSchema padre exista
        RecordSchema schema = schemaRepository.findById(recordSchemaId)
                .orElseThrow(() -> new EntityNotFoundException("RecordSchema no encontrado con id: " + recordSchemaId));

        // 2. Validar que no exista un atributo con el mismo nombre en este esquema
        attributeRepository.findByRecordSchemaIdAndName(recordSchemaId, request.getName())
                .ifPresent(attr -> {
                    throw new IllegalStateException("El atributo '" + request.getName() + "' ya existe en este esquema.");
                });

        // 3. Validar que el DataType exista
        DataType dataType = dataTypeRepository.findById(request.getDataTypeId())
                .orElseThrow(() -> new EntityNotFoundException("DataType no encontrado con id: " + request.getDataTypeId()));

        // 4. Validar que el Catalog exista (si se proporcionó)
        Catalog catalog = null;
        if (request.getCatalogId() != null) {
            catalog = catalogRepository.findById(request.getCatalogId())
                    .orElseThrow(() -> new EntityNotFoundException("Catalog no encontrado con id: " + request.getCatalogId()));
        }

        // 5. Crear y guardar la nueva entidad
        RecordSchemaAttribute newAttribute = RecordSchemaAttribute.builder()
                .name(request.getName())
                .isRequired(request.getIsRequired())
                .allowMultiple(request.getAllowMultiple())
                .recordSchema(schema)
                .dataType(dataType)
                .catalog(catalog)
                .build();

        return attributeRepository.save(newAttribute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordSchemaAttribute> findAttributesBySchemaId(String recordSchemaId) {
        return attributeRepository.findByRecordSchemaId(recordSchemaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecordSchemaAttribute> findAttributeById(String attributeId) {
        return attributeRepository.findById(attributeId);
    }

    @Override
    @Transactional
    public RecordSchemaAttribute updateAttribute(String attributeId, UpdateAttributeRequest request) {
        RecordSchemaAttribute existingAttribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Atributo no encontrado con id: " + attributeId));
        
        // Aquí iría la lógica completa de validación para la actualización...

        existingAttribute.setName(request.getName());
        existingAttribute.setRequired(request.getIsRequired());
        existingAttribute.setAllowMultiple(request.getAllowMultiple());
        // Aquí se actualizarían también dataType y catalog si fuera necesario...

        return attributeRepository.save(existingAttribute);
    }

    @Override
    @Transactional
    public void removeAttribute(String attributeId) {
        if (!attributeRepository.existsById(attributeId)) {
            throw new EntityNotFoundException("Atributo no encontrado con id: " + attributeId);
        }
        attributeRepository.deleteById(attributeId);
    }
}