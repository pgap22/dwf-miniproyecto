package sv.edu.udb.data_collector.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.service.ValidationRuleService;
import sv.edu.udb.data_collector.service.mapper.ValidationRuleMapper;

@RestController
@RequestMapping("/api/validation-rules")
@RequiredArgsConstructor
@Validated
public class ValidationRuleController {

    private final ValidationRuleService service;
    private final ValidationRuleMapper mapper;

    @GetMapping
    public List<ValidationRuleResponse> list() {
        return mapper.toResponseList(service.findAll());
    }

    @GetMapping("/{id}")
    public ValidationRuleResponse get(@PathVariable String id) {
        return mapper.toResponse(service.findById(id));
    }
}
