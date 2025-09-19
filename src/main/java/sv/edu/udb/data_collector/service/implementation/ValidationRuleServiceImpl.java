package sv.edu.udb.data_collector.service.implementation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.domain.ValidationRule;
import sv.edu.udb.data_collector.repository.ValidationRuleRepository;
import sv.edu.udb.data_collector.service.ValidationRuleService;
import sv.edu.udb.data_collector.service.mapper.ValidationRuleMapper;

@Service
@RequiredArgsConstructor
public class ValidationRuleServiceImpl implements ValidationRuleService {

    private final ValidationRuleRepository repository;
    private final ValidationRuleMapper mapper;

    @Override
    public List<ValidationRuleResponse> findAll() {
        List<ValidationRule> rules = repository.findAll();
        return mapper.toResponseList(rules);
    }

    @Override
    public ValidationRuleResponse findById(String id) {
        ValidationRule rule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        return mapper.toResponse(rule);
    }
}