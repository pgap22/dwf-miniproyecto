package sv.edu.udb.data_collector.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordSchemaRequestUpdate {
    private String name;
    private String description;
}