package sv.edu.udb.data_collector.controller.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordValueResponse {
    private String attributeId;
    private String attributeName;
    private String stringValue;
    private BigDecimal numberValue;
    private Boolean booleanValue;
    private OffsetDateTime dateValue;

    private String catalogItemId;   // antes: Long
}
