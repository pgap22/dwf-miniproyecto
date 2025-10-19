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
import sv.edu.udb.data_collector.configuration.RestExceptionHandler;
import sv.edu.udb.data_collector.controller.request.ChangeRoleRequest;
import sv.edu.udb.data_collector.controller.request.InviteMemberRequest;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.MemberRole;
import sv.edu.udb.data_collector.security.SecurityConfig;
import sv.edu.udb.data_collector.security.jwt.JwtAuthenticationFilter;
import sv.edu.udb.data_collector.service.WorkspaceUserService;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = WorkspaceMembershipController.class,
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
class WorkspaceMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkspaceUserService membershipService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/workspaces/{id}/members - Debe listar los miembros del workspace")
    void list_shouldReturnMembersList() throws Exception {
        // Arrange
        String workspaceId = "ws-1";
        MemberResponse member = MemberResponse.builder().id("user-1").email("test@example.com").build();
        given(membershipService.list(workspaceId)).willReturn(Collections.singletonList(member));

        // Act & Assert
        mockMvc.perform(get("/api/workspaces/{workspaceId}/members", workspaceId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("user-1")));
    }

    @Test
    @DisplayName("POST /api/workspaces/{id}/members/invite - Debe invitar a un nuevo miembro")
    void invite_shouldReturnCreatedMember() throws Exception {
        // Arrange
        String workspaceId = "ws-1";
        var dto = new InviteMemberRequest();
        dto.setEmail("new@example.com");
        InviteMemberRequest request = new InviteMemberRequest();
        MemberResponse response = MemberResponse.builder().id("user-2").email("new@example.com").role(MemberRole.MEMBER).build();

        given(membershipService.invite(workspaceId, "new@example.com")).willReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/workspaces/{workspaceId}/members/invite", workspaceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.role", is("MEMBER")));
    }

    @Test
    @DisplayName("PATCH /api/workspaces/{id}/members/{id}/role - Debe cambiar el rol de un miembro")
    void changeRole_shouldReturnActionResponse() throws Exception {
        // Arrange
        String workspaceId = "ws-1";
        String userId = "user-1";
        ChangeRoleRequest request = new ChangeRoleRequest("ADMIN");

        // --- CAMBIO PRINCIPAL AQUÍ ---
        // Ahora simulamos que el servicio devuelve 'true' para indicar éxito.
        given(membershipService.changeMemberRole(workspaceId, userId, MemberRole.ADMIN)).willReturn(true);

        // Act & Assert
        mockMvc.perform(patch("/api/workspaces/{workspaceId}/members/{userId}/role", workspaceId, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("ROLE_UPDATED")))
                .andExpect(jsonPath("$.userId", is(userId)))
                .andExpect(jsonPath("$.newRole", is("ADMIN")));
    }

    @Test
    @DisplayName("DELETE /api/workspaces/{id}/members/{id} - Debe remover a un miembro")
    void removeMember_shouldReturnActionResponse() throws Exception {
        // Arrange
        String workspaceId = "ws-1";
        String userId = "user-1";
        
        // --- CAMBIO PRINCIPAL AQUÍ ---
        // También simulamos que el servicio devuelve 'true'.
        given(membershipService.removeMember(workspaceId, userId)).willReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/members/{userId}", workspaceId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action", is("MEMBER_REMOVED")))
                .andExpect(jsonPath("$.userId", is(userId)));
    }
}