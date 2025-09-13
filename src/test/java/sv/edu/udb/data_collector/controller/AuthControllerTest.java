package sv.edu.udb.data_collector.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.udb.data_collector.controller.request.LoginRequest;
import sv.edu.udb.data_collector.controller.response.LoginResponse;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad para este slice
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockBean AuthService authService;

    @Test
    void login_ok_200() throws Exception {
        var user = UserResponse.builder().id("u1").name("Ana").email("ana@p.com").build();
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(LoginResponse.builder().token("jwt-token").user(user).build());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@p.com\",\"password\":\"Secreta1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("ana@p.com"));
    }

    @Test
    void login_400_por_validacion() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mal\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
