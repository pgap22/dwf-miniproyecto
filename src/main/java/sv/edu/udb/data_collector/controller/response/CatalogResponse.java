package sv.edu.udb.data_collector.controller.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CatalogResponse {
    String id;
    String name;
    String description;
    String workspaceId;
    Instant createdAt;
    Instant updatedAt;
}
