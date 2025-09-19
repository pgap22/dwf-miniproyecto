package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CatalogCreateRequest {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String workspaceId; 
}
