package sv.edu.udb.data_collector.controller.response;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordResponse {
    private Long id;
    private String schemaId;                // <-- era Long
    private OffsetDateTime createdAt;
    private UserResponse createdBy;
    private List<RecordValueResponse> values;
}
