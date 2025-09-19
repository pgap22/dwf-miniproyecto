package sv.edu.udb.data_collector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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
import static org.assertj.core.api.Assertions.assertThat;

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

    // ---------- GIVEN / EXPECTED ----------
    private static final String EMAIL = "ana@p.com";
    private static final String PASSWORD = "Secreta1";
    private static final String BAD_EMAIL = "mal";
    private static final String BAD_PASSWORD = "";

    private static final String TOKEN = "jwt-token";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @MockBean
    AuthService authService;

    @Test
    @DisplayName("POST /api/login → 200 con token y usuario")
    void login_ok_200() throws Exception {
        // ---------- Arrange ----------
        var expectedUser = UserResponse.builder()
                .id("u1").name("Ana").email(EMAIL).build();
        var expectedResponse = LoginResponse.builder()
                .token(TOKEN)
                .user(expectedUser)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        var payload = MAPPER.writeValueAsString(
                new LoginRequest(EMAIL, PASSWORD)
        );

        // ---------- Act ----------
        var result = mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        // ---------- Assert ----------
        var resp = result.getResponse();
        var status = resp.getStatus();
        var body = resp.getContentAsString();

        assertThat(status).isEqualTo(200);
        assertThat(resp.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        // Validación por JSONPath para facilitar debug puntual
        mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.user.email").value(EMAIL));
    }

    @Test
    @DisplayName("POST /api/login → 400 cuando la validación falla")
    void login_400_por_validacion() throws Exception {
        // ---------- Arrange ----------
        var invalidPayload = """
            {"email":"%s","password":"%s"}
        """.formatted(BAD_EMAIL, BAD_PASSWORD);

        // ---------- Act ----------
        var result = mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andReturn();

        // ---------- Assert ----------
        var resp = result.getResponse();
        assertThat(resp.getStatus()).isEqualTo(400);
    }
}
