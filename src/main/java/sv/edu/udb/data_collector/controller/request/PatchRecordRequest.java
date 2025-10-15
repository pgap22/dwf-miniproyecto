package sv.edu.udb.data_collector.controller.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchRecordRequest {
    // Valores a crear/actualizar (upsert)
    private List<CreateRecordValueRequest> values;

    // Eliminar valores de estos atributos del record
    private List<String> removeAttributeIds;
}
