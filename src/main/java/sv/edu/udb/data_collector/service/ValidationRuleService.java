package sv.edu.udb.data_collector.service;

import java.util.List;

import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;

public interface ValidationRuleService {
    List<ValidationRuleResponse> findAll();
    ValidationRuleResponse findById(String id);
}