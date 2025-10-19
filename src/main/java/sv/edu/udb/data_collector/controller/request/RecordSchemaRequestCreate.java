package sv.edu.udb.data_collector.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordSchemaRequestCreate {
    @NotBlank
    private String workspaceId;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @Size(max = 255)
    private String description;
}