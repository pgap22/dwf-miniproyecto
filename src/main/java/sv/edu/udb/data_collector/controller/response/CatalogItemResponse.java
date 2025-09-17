package sv.edu.udb.data_collector.controller.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CatalogItemResponse {
    String id;
    String catalogId;
    String value;
    Instant createdAt;
    Instant updatedAt;
}
