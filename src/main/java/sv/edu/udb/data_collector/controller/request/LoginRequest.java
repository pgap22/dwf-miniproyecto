package sv.edu.udb.data_collector.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;
}
