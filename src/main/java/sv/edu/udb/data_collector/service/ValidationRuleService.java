package sv.edu.udb.data_collector.service;

import java.util.List;

import sv.edu.udb.data_collector.domain.ValidationRule;

public interface ValidationRuleService {
    List<ValidationRule> findAll();
    ValidationRule findById(String id);
}
