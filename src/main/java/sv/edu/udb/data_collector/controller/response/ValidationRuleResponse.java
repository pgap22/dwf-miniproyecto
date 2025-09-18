package sv.edu.udb.data_collector.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class ValidationRuleResponse {
    private String id;
    private String name;
}
