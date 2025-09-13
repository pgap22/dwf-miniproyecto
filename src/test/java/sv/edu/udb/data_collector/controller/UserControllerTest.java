package sv.edu.udb.data_collector.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.NoSuchElementException;

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

import jakarta.persistence.EntityNotFoundException;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.UserService;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;

import sv.edu.udb.data_collector.configuration.RestExceptionHandler;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { SecurityConfig.class,
        JwtAuthenticationFilter.class }))
@Import(RestExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    private <T> T readJson(String json, Class<T> type) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, type);
    }

    @Test
    void get_by_id_ok() throws Exception {
        // Arrange
        final String id = "1";
        final String url = "/users/" + id;
        var expected = new UserResponse("1", "Bart", "bart@pukis.com");
        when(userService.findById(id)).thenReturn(expected);

        // Act
        var result = mvc.perform(get(url).accept(APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // Assert
        var status = result.getResponse().getStatus();
        var body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(200);

        var actual = readJson(body, UserResponse.class);
        assertThat(actual.getId()).isEqualTo("1");
        assertThat(actual.getName()).isEqualTo("Bart");
        assertThat(actual.getEmail()).isEqualTo("bart@pukis.com");
    }

    @Test
    void list_ok() throws Exception {
        // Arrange
        var expected = java.util.List.of(
                new UserResponse("1", "A", "a@a.com"),
                new UserResponse("2", "B", "b@b.com"));
        when(userService.list()).thenReturn(expected);

        // Act
        var result = mvc.perform(get("/users").accept(APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // Assert
        var status = result.getResponse().getStatus();
        var body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(200);

        var actual = new com.fasterxml.jackson.core.type.TypeReference<java.util.List<UserResponse>>() {
        };
        var list = new com.fasterxml.jackson.databind.ObjectMapper().readValue(body, actual);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getEmail()).isEqualTo("a@a.com");
        assertThat(list.get(1).getEmail()).isEqualTo("b@b.com");
    }

    @Test
    void create_returns_200_and_body() throws Exception {
        // Arrange
        var expected = new UserResponse("1", "Bart", "bart@pukis.com");
        when(userService.create(org.mockito.ArgumentMatchers.any(UserRequest.class)))
                .thenReturn(expected);

        var payload = """
                    {"name":"Bart","email":"bart@pukis.com","password":"123"}
                """;

        // Act
        var result = mvc.perform(post("/users")
                .contentType(APPLICATION_JSON)
                .content(payload)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // Assert
        var status = result.getResponse().getStatus();
        var body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(200);

        var actual = readJson(body, UserResponse.class);
        assertThat(actual.getId()).isEqualTo("1");
        assertThat(actual.getEmail()).isEqualTo("bart@pukis.com");
    }

    @Test
    void create_400_validation() throws Exception {
        // Arrange
        var invalid = """
                    {"name":"","email":"mal","password":""}
                """;

        // Act
        var result = mvc.perform(post("/users")
                .contentType(APPLICATION_JSON)
                .content(invalid)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // Assert
        var status = result.getResponse().getStatus();
        var body = result.getResponse().getContentAsString();

        assertThat(status).isEqualTo(400);
        // Si tu ControllerAdvice devuelve detalles de validación, puedes asertarlos:
        assertThat(body).contains("ViolationFieldError");
        assertThat(body).contains("email");
    }

    @Test
    void get_by_id_404() throws Exception {
        // Arrange
        final String id = "999";
        final String url = "/users/" + id;
        final int expectedStatus = 404;

        when(userService.findById(id))
                .thenThrow(new EntityNotFoundException("not found"));

        // Act
        var resultActions = mvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()); // 👈 imprime request/response (útil al fallar)

        // …o bien captura el resultado para debugear variables:
        var result = resultActions.andReturn();
        var actualStatus = result.getResponse().getStatus();
        var body = result.getResponse().getContentAsString();

        // Breakpoints fáciles aquí:
        assertThat(body).contains("not found"); // si tu handler lo incluye
        assertThat(actualStatus).isEqualTo(expectedStatus);
        // puedes inspeccionar body en el depurador:
    }
}