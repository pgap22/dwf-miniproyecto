package sv.edu.udb.data_collector.controller.request;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRecordValueRequest {

    @NotBlank
    private String attributeId;

    // Solo uno de estos debe venir según el tipo del atributo
    private String stringValue;
    private BigDecimal numberValue;
    private Boolean booleanValue;
    private OffsetDateTime dateValue;

    // Para atributos de catálogo (IDs String en tu proyecto)
    private String catalogItemId;
}
