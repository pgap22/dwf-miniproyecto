package sv.edu.udb.data_collector.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRecordRequest {
    @NotBlank(message = "Debes añadir el schemaId")
    private String schemaId;

    @NotNull(message = "Debes añadir un json")
    private JsonNode data;    
}

