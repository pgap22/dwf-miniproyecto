package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CatalogItemCreateRequest {
    @NotBlank
    private String value;
}
