package sv.edu.udb.data_collector.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.data_collector.domain.RecordScheme;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.repository.RecordSchemeRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.RecordSchemeService;

import java.util.List;
import java.util.Optional;

@Service // Anotación para que Spring lo reconozca como un bean de servicio
@RequiredArgsConstructor // Lombok: crea un constructor con los campos 'final'
public class RecordSchemeServiceImpl implements RecordSchemeService {

    // Inyección de dependencias a través del constructor (manejado por Lombok)
    private final RecordSchemeRepository recordSchemeRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional // Asegura que toda la operación se ejecute en una sola transacción
    public RecordScheme create(String workspaceId, String name, String description) {
        // 1. Validar que el Workspace exista
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace no encontrado con el id: " + workspaceId));

        // 2. Validar que no exista un esquema con el mismo nombre en ese workspace
        recordSchemeRepository.findByWorkspaceIdAndName(workspaceId, name)
                .ifPresent(scheme -> {
                    throw new IllegalStateException("Ya existe un esquema con el nombre '" + name + "' en este workspace.");
                });

        // 3. Crear y guardar la nueva entidad
        RecordScheme newScheme = RecordScheme.builder()
                .name(name)
                .description(description)
                .workspace(workspace)
                .build();

        return recordSchemeRepository.save(newScheme);
    }

    @Override
    @Transactional(readOnly = true) // Optimización para operaciones de solo lectura
    public List<RecordScheme> findAllByWorkspaceId(String workspaceId) {
        return recordSchemeRepository.findByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecordScheme> findById(String id) {
        return recordSchemeRepository.findById(id);
    }

    @Override
    @Transactional
    public RecordScheme update(String id, RecordScheme updatedData) {
        // 1. Encontrar el esquema existente o lanzar una excepción si no se encuentra
        RecordScheme existingScheme = recordSchemeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RecordScheme no encontrado con el id: " + id));

        // 2. Validar si el nombre ha cambiado y si el nuevo nombre ya está en uso
        if (!existingScheme.getName().equals(updatedData.getName())) {
             recordSchemeRepository.findByWorkspaceIdAndName(existingScheme.getWorkspace().getId(), updatedData.getName())
                .ifPresent(scheme -> {
                    throw new IllegalStateException("El nombre '" + updatedData.getName() + "' ya está en uso en este workspace.");
                });
        }
        
        // 3. Actualizar los campos permitidos
        existingScheme.setName(updatedData.getName());
        existingScheme.setDescription(updatedData.getDescription());

        // 4. Guardar los cambios (JPA lo hace automáticamente al final de la transacción, 
        // pero es buena práctica llamarlo explícitamente para mayor claridad)
        return recordSchemeRepository.save(existingScheme);
    }

    @Override
    @Transactional
    public void delete(String id) {
        // 1. Verificar si el esquema existe antes de intentar borrarlo
        if (!recordSchemeRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar. RecordScheme no encontrado con el id: " + id);
        }
        // 2. Eliminar la entidad
        recordSchemeRepository.deleteById(id);
    }
}