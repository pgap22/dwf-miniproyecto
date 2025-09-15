package sv.edu.udb.data_collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.data_collector.controller.request.InviteMemberRequest;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.service.MembershipService;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMembershipController {

    private final MembershipService membershipService;

    @GetMapping
    public List<MemberResponse> list(@PathVariable String workspaceId) {
        return membershipService.list(workspaceId);
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse invite(@PathVariable String workspaceId,
                                 @Valid @RequestBody InviteMemberRequest request) {
        return membershipService.invite(workspaceId, request);
    }
}
