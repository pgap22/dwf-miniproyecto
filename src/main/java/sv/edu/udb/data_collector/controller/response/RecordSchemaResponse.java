package sv.edu.udb.data_collector.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordSchemaResponse {

    private String id;
    private String name;
    private String description;
    
    // Solo exponemos el ID del workspace, no el objeto completo.
    private String workspaceId; 
}