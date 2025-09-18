package sv.edu.udb.data_collector.service.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import sv.edu.udb.data_collector.controller.response.ValidationRuleResponse;
import sv.edu.udb.data_collector.domain.ValidationRule;

@Mapper(componentModel = "spring")
public interface ValidationRuleMapper {
    ValidationRuleResponse toResponse(ValidationRule entity);
    List<ValidationRuleResponse> toResponseList(List<ValidationRule> list);
}
