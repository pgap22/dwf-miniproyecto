package sv.edu.udb.data_collector.controller.request;

import lombok.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateRecordRequest {
    @NotEmpty
    private List<CreateRecordValueRequest> values;
}
