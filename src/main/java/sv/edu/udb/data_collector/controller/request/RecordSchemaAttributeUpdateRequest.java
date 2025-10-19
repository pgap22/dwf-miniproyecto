package sv.edu.udb.data_collector.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordSchemaAttributeUpdateRequest {

    private String name;
    
    private Boolean isRequired;
    
    private Boolean allowMultiple;
    
    private String dataTypeId;
    
    private String catalogId;
}