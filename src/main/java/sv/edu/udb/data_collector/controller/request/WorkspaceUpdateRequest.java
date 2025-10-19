package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceUpdateRequest {
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;
}
