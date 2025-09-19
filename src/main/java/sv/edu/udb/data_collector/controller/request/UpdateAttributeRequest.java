package sv.edu.udb.data_collector.controller.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateAttributeRequest {

    private String name;
    
    private Boolean isRequired;
    
    private Boolean allowMultiple;
    
    private String dataTypeId;
    
    private String catalogId;
}