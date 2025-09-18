package sv.edu.udb.data_collector.controller.request;

import lombok.Data;

@Data
public class UpdateRecordSchemaRequest {
    private String name;
    private String description;
}