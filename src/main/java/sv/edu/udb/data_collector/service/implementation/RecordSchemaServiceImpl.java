package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestCreate;
import sv.edu.udb.data_collector.controller.request.RecordSchemaRequestUpdate;
import sv.edu.udb.data_collector.controller.response.RecordSchemaResponse;
import sv.edu.udb.data_collector.domain.RecordSchema;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.RecordSchemaRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.RecordSchemaService;
import sv.edu.udb.data_collector.service.mapper.RecordSchemaMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordSchemaServiceImpl implements RecordSchemaService {

    private final RecordSchemaRepository recordSchemeRepository;
    private final WorkspaceRepository workspaceRepository;
    private final RecordSchemaMapper recordSchemaMapper;

    @Transactional
    public RecordSchemaResponse create(RecordSchemaRequestCreate request) {
        // 1. Obtener el workspace
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace no encontrado con el id: " + request.getWorkspaceId()));

        // 2. Validar que no exista un esquema con el mismo nombre en ese workspace
        recordSchemeRepository.findByWorkspaceIdAndName(request.getWorkspaceId(), request.getName())
                .ifPresent(scheme -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un esquema con el nombre '" + request.getName() + "' en este workspace.");
                });

        // 3. Mapear el DTO a la entidad y asignar el workspace
        RecordSchema newScheme = recordSchemaMapper.toRecordSchema(request);
        newScheme.setWorkspace(workspace);

        // 4. Guardar y mapear la entidad de vuelta a un DTO de respuesta
        RecordSchema savedScheme = recordSchemeRepository.save(newScheme);
        return recordSchemaMapper.toResponse(savedScheme);
    }

    @Transactional(readOnly = true)
    public List<RecordSchemaResponse> findAllByWorkspaceId(String workspaceId) {
        List<RecordSchema> schemes = recordSchemeRepository.findByWorkspaceId(workspaceId);
        return recordSchemaMapper.toResponseList(schemes);
    }

    @Transactional(readOnly = true)
    public RecordSchemaResponse findById(String id) {
        RecordSchema scheme = recordSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RecordSchema no encontrado con el id: " + id));
        return recordSchemaMapper.toResponse(scheme);
    }

    @Transactional
    public RecordSchemaResponse update(String id, RecordSchemaRequestUpdate request) {
        // 1. Encontrar la entidad existente
        RecordSchema existingScheme = recordSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RecordSchema no encontrado con el id: " + id));

        // 2. Validar si el nombre ha cambiado y si el nuevo nombre ya está en uso
        if (request.getName() != null && !request.getName().equals(existingScheme.getName())) {
             recordSchemeRepository.findByWorkspaceIdAndName(existingScheme.getWorkspace().getId(), request.getName())
                .ifPresent(scheme -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre '" + request.getName() + "' ya está en uso en este workspace.");
                });
        }
        
        // 3. Mapear los campos permitidos del DTO a la entidad
        recordSchemaMapper.updateRecordSchema(request, existingScheme);

        // 4. Guardar y mapear la entidad de vuelta a un DTO de respuesta
        RecordSchema updatedScheme = recordSchemeRepository.save(existingScheme);
        return recordSchemaMapper.toResponse(updatedScheme);
    }

    @Transactional
    public void delete(String id) {
        recordSchemeRepository.deleteById(id);
    }
}