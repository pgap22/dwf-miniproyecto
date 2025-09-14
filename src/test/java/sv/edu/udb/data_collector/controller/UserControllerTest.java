package sv.edu.udb.data_collector.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.List;

import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(
        controllers = UserController.class,
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
class UserControllerTest {

    // ---------- GIVEN / EXPECTED ----------
    private static final String USER_ID_1 = "1";
    private static final String USER_ID_404 = "999";

    private static final String USER_NAME_BART = "Bart";
    private static final String EMAIL_BART = "bart@pukis.com";

    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    private <T> T readJson(String json, Class<T> type) throws Exception {
        return MAPPER.readValue(json, type);
    }

    private <T> T readList(String json, TypeReference<T> typeRef) throws Exception {
        return MAPPER.readValue(json, typeRef);
    }

    @Test
    @DisplayName("GET /users/{id} → 200 y body esperado")
    void get_by_id_ok() throws Exception {
        // ---------- Arrange ----------
        String id = USER_ID_1;
        String url = "/users/" + id;
        UserResponse expected = new UserResponse(USER_ID_1, USER_NAME_BART, EMAIL_BART);
        when(userService.findById(id)).thenReturn(expected);

        // ---------- Act ----------
        var result = mvc.perform(get(url).accept(APPLICATION_JSON))
                .andDo(print()) // útil para depurar si falla
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();

        assertThat(status).isEqualTo(HTTP_OK);

        UserResponse actual = readJson(body, UserResponse.class);
        assertThat(actual.getId()).isEqualTo(USER_ID_1);
        assertThat(actual.getName()).isEqualTo(USER_NAME_BART);
        assertThat(actual.getEmail()).isEqualTo(EMAIL_BART);
    }

    @Test
    @DisplayName("GET /users → 200 y lista")
    void list_ok() throws Exception {
        // ---------- Arrange ----------
        List<UserResponse> expected = List.of(
                new UserResponse("1", "A", "a@a.com"),
                new UserResponse("2", "B", "b@b.com")
        );
        when(userService.list()).thenReturn(expected);

        // ---------- Act ----------
        var result = mvc.perform(get("/users").accept(APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_OK);

        var listType = new TypeReference<List<UserResponse>>() {};
        List<UserResponse> actual = readList(body, listType);

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getEmail()).isEqualTo("a@a.com");
        assertThat(actual.get(1).getEmail()).isEqualTo("b@b.com");
    }

    @Test
    @DisplayName("POST /users → 200 y body creado")
    void create_returns_200_and_body() throws Exception {
        // ---------- Arrange ----------
        UserResponse expected = new UserResponse(USER_ID_1, USER_NAME_BART, EMAIL_BART);
        when(userService.create(any(UserRequest.class))).thenReturn(expected);

        String payload = """
            {"name":"Bart","email":"bart@pukis.com","password":"123"}
        """;

        // ---------- Act ----------
        var result = mvc.perform(
                        post("/users")
                                .contentType(APPLICATION_JSON)
                                .content(payload)
                                .accept(APPLICATION_JSON)
                )
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        assertThat(status).isEqualTo(HTTP_OK);

        UserResponse actual = readJson(body, UserResponse.class);
        assertThat(actual.getId()).isEqualTo(USER_ID_1);
        assertThat(actual.getEmail()).isEqualTo(EMAIL_BART);
    }

    @Test
    @DisplayName("POST /users → 400 cuando la validación falla")
    void create_400_validation() throws Exception {
        // ---------- Arrange ----------
        String invalid = """
            {"name":"","email":"mal","password":""}
        """;

        // ---------- Act ----------
        var result = mvc.perform(
                        post("/users")
                                .contentType(APPLICATION_JSON)
                                .content(invalid)
                                .accept(APPLICATION_JSON)
                )
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();

        assertThat(status).isEqualTo(HTTP_BAD_REQUEST);
        // Ajusta estos contains a lo que devuelva tu RestExceptionHandler:
        assertThat(body).contains("ViolationFieldError");
        assertThat(body).contains("email");
    }

    @Test
    @DisplayName("GET /users/{id} → 404 cuando no existe")
    void get_by_id_404() throws Exception {
        // ---------- Arrange ----------
        String id = USER_ID_404;
        String url = "/users/" + id;
        when(userService.findById(id)).thenThrow(new EntityNotFoundException("not found"));

        // ---------- Act ----------
        var result = mvc.perform(get(url).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        // ---------- Assert ----------
        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();

        assertThat(status).isEqualTo(HTTP_NOT_FOUND);
        // Si tu handler incluye el mensaje:
        assertThat(body).contains("not found");
    }
}
