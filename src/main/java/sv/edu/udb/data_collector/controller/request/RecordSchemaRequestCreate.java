package sv.edu.udb.data_collector.controller.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data 
public class RecordSchemaRequestCreate {
    @NotBlank
    private String workspaceId;

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;
    
    @Size(max = 255)
    private String description;
}