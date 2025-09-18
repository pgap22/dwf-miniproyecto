package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ValidationRuleRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
}
