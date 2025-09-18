package sv.edu.udb.data_collector.service.implementation;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sv.edu.udb.data_collector.controller.request.ValidationRuleRequest;
import sv.edu.udb.data_collector.domain.ValidationRule;
import sv.edu.udb.data_collector.repository.ValidationRuleRepository;
import sv.edu.udb.data_collector.service.ValidationRuleService;

@Service
@RequiredArgsConstructor
public class ValidationRuleServiceImpl implements ValidationRuleService {

    private final ValidationRuleRepository repository;

    @Override
    public List<ValidationRule> findAll() {
        // Si prefieres orden alfabético:
        // return repository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return repository.findAll();
    }

    @Override
    public ValidationRule findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
    }

    @Override
    public ValidationRule create(ValidationRuleRequest request) {
        String name = request.getName().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una regla con ese nombre");
        }
        ValidationRule entity = ValidationRule.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .build();
        try {
            return repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            // Respaldo por si choca con el índice único
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una regla con ese nombre");
        }
    }

    @Override
    public ValidationRule update(String id, ValidationRuleRequest request) {
        ValidationRule existing = findById(id);
        String newName = request.getName().trim();

        if (!existing.getName().equalsIgnoreCase(newName) && repository.existsByNameIgnoreCase(newName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una regla con ese nombre");
        }

        existing.setName(newName);
        try {
            return repository.save(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una regla con ese nombre");
        }
    }

    @Override
    public void delete(String id) {
        ValidationRule existing = findById(id);
        // Si hay FK desde attribute_validations y quieres bloquear el borrado:
        // throw new ResponseStatusException(HttpStatus.CONFLICT, "La regla está en uso");
        repository.delete(existing);
    }
}
