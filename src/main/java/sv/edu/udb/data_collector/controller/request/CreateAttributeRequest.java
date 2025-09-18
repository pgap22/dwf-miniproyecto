package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateAttributeRequest {

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String name;
    
    @NotNull(message = "El campo 'isRequired' es obligatorio.")
    private Boolean isRequired;
    
    @NotNull(message = "El campo 'allowMultiple' es obligatorio.")
    private Boolean allowMultiple;
    
    @NotBlank(message = "El 'dataTypeId' no puede estar vacío.")
    private String dataTypeId;
    
    // Opcional, puede ser nulo si el tipo de dato no es 'Catálogo'
    private String catalogId; 
}