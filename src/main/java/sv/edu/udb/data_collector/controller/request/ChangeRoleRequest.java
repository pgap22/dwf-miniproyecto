package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import sv.edu.udb.data_collector.controller.validation.ValueOfEnum;
import sv.edu.udb.data_collector.domain.MemberRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRoleRequest {

    @NotBlank(message = "role es requerido")
    @ValueOfEnum(enumClass = MemberRole.class, message = "role inválido")
    private String role;
}