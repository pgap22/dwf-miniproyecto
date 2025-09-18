package sv.edu.udb.data_collector.controller.response;

import lombok.Data;

@Data // Lombok para getters, setters, etc.
public class RecordSchemaResponse {

    private String id;
    private String name;
    private String description;
    
    // Solo exponemos el ID del workspace, no el objeto completo.
    private String workspaceId; 
}