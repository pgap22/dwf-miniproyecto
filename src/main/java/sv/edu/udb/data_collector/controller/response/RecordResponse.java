package sv.edu.udb.data_collector.controller.response;

import lombok.*;
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordResponse {
    private String id;
    private String data;
    private String userId;
    private String schemaId;
}
