package sv.edu.udb.data_collector.controller.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DataTypeResponse {
    private String id;
    private String name;
    private String kind;
}
