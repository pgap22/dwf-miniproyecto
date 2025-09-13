package sv.edu.udb.data_collector.controller.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String token;         // JWT
    private UserResponse user;    // Info pública del usuario
}