package sv.edu.udb.data_collector.controller.response;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
