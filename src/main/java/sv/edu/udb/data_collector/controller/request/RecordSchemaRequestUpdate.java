package sv.edu.udb.data_collector.controller.request;

import lombok.Data;

@Data
public class RecordSchemaRequestUpdate {
    private String name;
    private String description;
}