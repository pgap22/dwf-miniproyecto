package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.LoginRequest;
import sv.edu.udb.data_collector.controller.response.LoginResponse;
import sv.edu.udb.data_collector.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
