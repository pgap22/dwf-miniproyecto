package sv.edu.udb.data_collector.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔑 desactiva filtros de Spring Security
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Test
    void create_returns_200_and_body() throws Exception {
        when(userService.create(any(UserRequest.class)))
            .thenReturn(new UserResponse("1","Bart","bart@pukis.com"));

        String json = "{\"name\":\"Bart\",\"email\":\"bart@pukis.com\",\"password\":\"123\"}";

        mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.email").value("bart@pukis.com"));
    }

    @Test
    void get_by_id_ok() throws Exception {
        when(userService.findById("1")).thenReturn(new UserResponse("1","Bart","bart@pukis.com"));

        mvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bart"));
    }

    @Test
    void list_ok() throws Exception {
        when(userService.list()).thenReturn(List.of(
            new UserResponse("1","A","a@a.com"),
            new UserResponse("2","B","b@b.com")
        ));

        mvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("a@a.com"))
            .andExpect(jsonPath("$[1].email").value("b@b.com"));
    }
}
