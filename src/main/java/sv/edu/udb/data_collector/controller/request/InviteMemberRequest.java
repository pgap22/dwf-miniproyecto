package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import sv.edu.udb.data_collector.domain.MemberRole;

@Data
public class InviteMemberRequest {

    @NotBlank @Email
    private String email;

    // Opcional: si no viene, por defecto MEMBER
    private MemberRole role;
}
