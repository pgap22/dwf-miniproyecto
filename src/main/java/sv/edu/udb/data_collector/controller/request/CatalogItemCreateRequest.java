package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CatalogItemCreateRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String label;
    private Boolean isActive; // default true
}
