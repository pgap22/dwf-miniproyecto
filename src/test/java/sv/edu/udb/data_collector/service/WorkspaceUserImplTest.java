package sv.edu.udb.data_collector.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.EntityNotFoundException;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.repository.WorkspaceUserRepository;
import sv.edu.udb.data_collector.service.implementation.WorkspaceUserServiceImpl;
import sv.edu.udb.data_collector.service.mapper.WorkspaceUserMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceUserServiceImpl")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WorkspaceUserServiceImplTest {

    @Mock WorkspaceRepository workspaceRepository;
    @Mock UserRepository userRepository;
    @Mock WorkspaceUserRepository memberRepository;
    @Mock WorkspaceUserMapper mapper;

    @InjectMocks WorkspaceUserServiceImpl service;

    @Test
    @DisplayName("invite crea miembro cuando workspace y user existen")
    void invite_creates_member_when_ws_and_user_exist() {
        // Arrange
        var ws = Workspace.builder().id("ws-1").name("Data").build();
        var user = User.builder().id("user-1").email("alice@example.com").name("Alice").passwordHash("x").build();
        var member = WorkspaceUser.builder()
                .id("m-1").workspace(ws).user(user).role(MemberRole.MEMBER).createdAt(Instant.now()).build();
        var response = MemberResponse.builder()
                .id("m-1").userId("user-1").email("alice@example.com").name("Alice")
                .role(MemberRole.MEMBER.name()).joinedAt(member.getCreatedAt())
                .build();

        when(workspaceRepository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(memberRepository.save(any(WorkspaceUser.class))).thenReturn(member);
        when(mapper.toResponse(member)).thenReturn(response);

        // Act
        var result = service.invite("ws-1", "alice@example.com");

        // Assert
        assertThat(result).isEqualTo(response);
        verify(memberRepository).save(any(WorkspaceUser.class));
        verify(mapper).toResponse(member);
    }

    @Test
    @DisplayName("invite lanza 404 si workspace no existe")
    void invite_throws_404_when_workspace_not_found() {
        when(workspaceRepository.findById("ws-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.invite("ws-1", "alice@example.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workspace not found");
    }

    @Test
    @DisplayName("invite lanza 404 si user no existe")
    void invite_throws_404_when_user_not_found() {
        var ws = Workspace.builder().id("ws-1").name("Data").build();
        when(workspaceRepository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.invite("ws-1", "alice@example.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("invite propaga DataIntegrityViolationException si ya existe el miembro")
    void invite_rethrows_data_integrity_on_duplicate_member() {
        var ws = Workspace.builder().id("ws-1").name("Data").build();
        var user = User.builder().id("user-1").email("alice@example.com").name("Alice").passwordHash("x").build();

        when(workspaceRepository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(memberRepository.save(any(WorkspaceUser.class)))
                .thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.invite("ws-1", "alice@example.com"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("list devuelve miembros mapeados cuando workspace existe")
    void list_returns_mapped_members_when_workspace_exists() {
        var ws = Workspace.builder().id("ws-1").name("Data").build();
        var user = User.builder().id("u-1").email("a@e.com").name("A").passwordHash("x").build();
        var member = WorkspaceUser.builder()
                .id("m-1").workspace(ws).user(user).role(MemberRole.MEMBER).createdAt(Instant.now()).build();
        var dto = MemberResponse.builder()
                .id("m-1").userId("u-1").email("a@e.com").name("A")
                .role("MEMBER").joinedAt(member.getCreatedAt()).build();

        when(workspaceRepository.findById("ws-1")).thenReturn(Optional.of(ws));
        when(memberRepository.findByWorkspaceId("ws-1")).thenReturn(List.of(member));
        when(mapper.toResponse(member)).thenReturn(dto);

        var result = service.list("ws-1");

        assertThat(result).containsExactly(dto);
    }

    @Test
    @DisplayName("list lanza 404 si workspace no existe")
    void list_throws_404_when_workspace_not_found() {
        when(workspaceRepository.findById("ws-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list("ws-1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workspace not found");
    }

    @Test
    @DisplayName("changeMemberRole retorna true cuando actualiza 1 fila")
    void changeMemberRole_returns_true_when_one_row_updated() {
        when(memberRepository.updateRoleByWorkspaceIdAndUserId("ws-1", "u-1", MemberRole.ADMIN))
                .thenReturn(1);

        boolean ok = service.changeMemberRole("ws-1", "u-1", MemberRole.ADMIN);

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("changeMemberRole lanza 404 cuando no encuentra el miembro")
    void changeMemberRole_throws_404_when_member_not_found() {
        when(memberRepository.updateRoleByWorkspaceIdAndUserId("ws-1", "u-1", MemberRole.ADMIN))
                .thenReturn(0);

        assertThatThrownBy(() -> service.changeMemberRole("ws-1", "u-1", MemberRole.ADMIN))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("removeMember retorna true cuando elimina 1 fila")
    void removeMember_returns_true_when_one_row_deleted() {
        when(memberRepository.deleteByWorkspaceIdAndUserId("ws-1", "u-1"))
                .thenReturn(1L);

        boolean ok = service.removeMember("ws-1", "u-1");

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("removeMember lanza 404 cuando no encuentra el miembro")
    void removeMember_throws_404_when_member_not_found() {
        when(memberRepository.deleteByWorkspaceIdAndUserId("ws-1", "u-1"))
                .thenReturn(0L);

        assertThatThrownBy(() -> service.removeMember("ws-1", "u-1"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Member not found");
    }
}
