package sv.edu.udb.data_collector.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.AuthService;
import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.request.LoginRequest;
import sv.edu.udb.data_collector.controller.response.LoginResponse;
import sv.edu.udb.data_collector.controller.response.UserResponse;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = { SecurityConfig.class, JwtAuthenticationFilter.class }
    )
)
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;

    @MockBean AuthService authService;

    @Test
    void login_ok_200() throws Exception {
        var user = UserResponse.builder()
                .id("u1").name("Ana").email("ana@p.com").build();
        var resp = LoginResponse.builder()
                .token("jwt-token")
                .user(user)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@p.com\",\"password\":\"Secreta1\"}"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.token").value("jwt-token"))
           .andExpect(jsonPath("$.user.email").value("ana@p.com"));
    }

    @Test
    void login_400_por_validacion() throws Exception {
        // Asumiendo que tu LoginRequest tiene @Email y @NotBlank
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"mal\",\"password\":\"\"}"))
           .andExpect(status().isBadRequest());
    }
}
