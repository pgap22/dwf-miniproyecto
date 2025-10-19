package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogCreateRequest {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String workspaceId; 
}
