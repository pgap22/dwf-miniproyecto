package sv.edu.udb.data_collector.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // no enviar campos null
public class RecordSchemaAttributeResponse {

    private String id;
    private String name;
    private Boolean isRequired;
    private Boolean allowMultiple;
    private String recordSchemaId;
    private String dataTypeId;
    private String catalogId; // Puede ser nulo
    private DataTypeResponse datatype;
}