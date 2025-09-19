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
                    throw new IllegalStateException(
                            "El atributo '" + request.getName() + "' ya existe en este esquema.");
                });

        // 3. Validar que el DataType exista
        DataType dataType = dataTypeRepository.findById(request.getDataTypeId())
                .orElseThrow(
                        () -> new EntityNotFoundException("DataType no encontrado con id: " + request.getDataTypeId()));

        // 4. Validar que el Catalog exista (si se proporcionó)
        Catalog catalog = null;
        if (request.getCatalogId() != null) {
            catalog = catalogRepository.findById(request.getCatalogId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Catalog no encontrado con id: " + request.getCatalogId()));
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
        // 1. Obtener la entidad existente o lanzar una excepción si no se encuentra.
        RecordSchemaAttribute existingAttribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Atributo no encontrado con id: " + attributeId));

        // 2. Validar y actualizar el nombre, solo si se proporcionó.
        if (request.getName() != null) {
            // A) Validar que el nuevo nombre no esté en blanco.
            if (request.getName().isBlank()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío o en blanco.");
            }
            // B) Si el nombre ha cambiado, validar que no sea un duplicado.
            if (!request.getName().equals(existingAttribute.getName())) {
                attributeRepository.findByRecordSchemaIdAndName(
                        existingAttribute.getRecordSchema().getId(), request.getName()).ifPresent(attr -> {
                            throw new IllegalStateException("El nombre del atributo '" + request.getName()
                                    + "' ya está en uso en este esquema.");
                        });
            }
            existingAttribute.setName(request.getName());
        }

        // 3. Actualizar los campos booleanos, solo si se proporcionaron.
        if (request.getIsRequired() != null) {
            existingAttribute.setRequired(request.getIsRequired());
        }
        if (request.getAllowMultiple() != null) {
            existingAttribute.setAllowMultiple(request.getAllowMultiple());
        }

        // 4. Actualizar el DataType, solo si se proporcionó y es diferente.
        if (request.getDataTypeId() != null
                && !request.getDataTypeId().equals(existingAttribute.getDataType().getId())) {
            DataType newDataType = dataTypeRepository.findById(request.getDataTypeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "DataType no encontrado con id: " + request.getDataTypeId()));
            existingAttribute.setDataType(newDataType);
        }

        // 5. Actualizar o desvincular el Catalog.
        if (request.getCatalogId() != null) {
            // Caso 1: El cliente envía un string vacío para desvincular el catálogo.
            if (request.getCatalogId().isEmpty()) {
                existingAttribute.setCatalog(null);
            } 
            // Caso 2: El cliente envía un nuevo ID para vincular o cambiar el catálogo.
            else {
                // Se busca el nuevo catálogo solo si el ID es diferente al actual.
                if (existingAttribute.getCatalog() == null || !request.getCatalogId().equals(existingAttribute.getCatalog().getId())) {
                    Catalog newCatalog = catalogRepository.findById(request.getCatalogId())
                            .orElseThrow(() -> new EntityNotFoundException("Catalog no encontrado con id: " + request.getCatalogId()));
                    existingAttribute.setCatalog(newCatalog);
                }
            }
        }

        // 6. Guardar y devolver la entidad actualizada.
        RecordSchemaAttribute savedAttribute = attributeRepository.save(existingAttribute);
        
        return savedAttribute;
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