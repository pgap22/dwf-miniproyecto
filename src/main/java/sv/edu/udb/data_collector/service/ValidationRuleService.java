package sv.edu.udb.data_collector.service;

import java.util.List;

import sv.edu.udb.data_collector.controller.request.ValidationRuleRequest;
import sv.edu.udb.data_collector.domain.ValidationRule;

public interface ValidationRuleService {
    List<ValidationRule> findAll();
    ValidationRule findById(String id);
    ValidationRule create(ValidationRuleRequest request);
    ValidationRule update(String id, ValidationRuleRequest request);
    void delete(String id);
}
