package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sv.edu.udb.data_collector.controller.request.ChangeRoleRequest;
import sv.edu.udb.data_collector.controller.request.InviteMemberRequest;
import sv.edu.udb.data_collector.controller.response.MemberActionResponse;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.MemberRole;
import sv.edu.udb.data_collector.service.WorkspaceUserService;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMembershipController {

    private final WorkspaceUserService membershipService;

    @GetMapping
    public List<MemberResponse> list(@PathVariable String workspaceId) {
        return membershipService.list(workspaceId);
    }

    @PostMapping("/invite")
    public ResponseEntity<MemberResponse> invite(
            @PathVariable String workspaceId,
            @Valid @RequestBody InviteMemberRequest request) {

        var response = membershipService.invite(workspaceId, request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<MemberActionResponse> changeRole(
            @PathVariable String workspaceId,
            @PathVariable String userId,
            @Valid @RequestBody ChangeRoleRequest request) {

        MemberRole role = MemberRole.valueOf(request.getRole().toUpperCase(Locale.ROOT));

        membershipService.changeMemberRole(workspaceId, userId, role);

        var body = MemberActionResponse.builder()
                .action("ROLE_UPDATED")
                .workspaceId(workspaceId)
                .userId(userId)
                .newRole(role)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<MemberActionResponse> removeMember(
            @PathVariable String workspaceId,
            @PathVariable String userId) {

        membershipService.removeMember(workspaceId, userId);

        var body = MemberActionResponse.builder()
                .action("MEMBER_REMOVED")
                .workspaceId(workspaceId)
                .userId(userId)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(body); // 200 con detalles
    }
}
