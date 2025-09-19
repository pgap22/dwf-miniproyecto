package sv.edu.udb.data_collector.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.service.ValidationRuleService;

@RestController
@RequestMapping("/api/validation-rules")
@RequiredArgsConstructor
@Validated
public class ValidationRuleController {

    private final ValidationRuleService service;

    @GetMapping
    public List<ValidationRuleResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ValidationRuleResponse get(@PathVariable String id) {
        return service.findById(id);
    }
}
