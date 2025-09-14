package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceUpdateRequest {
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;
}
