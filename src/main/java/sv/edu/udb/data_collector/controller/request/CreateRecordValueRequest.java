package sv.edu.udb.data_collector.controller.request;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateRecordValueRequest {

    @NotBlank
    private String attributeId;

    private String stringValue;
    private BigDecimal numberValue;
    private Boolean booleanValue;
    private OffsetDateTime dateValue;

    private String catalogItemId;   // antes: Long
}
