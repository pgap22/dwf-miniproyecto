package sv.edu.udb.data_collector.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // no enviar campos null
public class AttributeResponse {

    private String id;
    private String name;
    private boolean isRequired;
    private boolean allowMultiple;
    private String recordSchemaId;
    private String dataTypeId;
    private String catalogId; // Puede ser nulo
    private DataTypeResponse datatype;
}