package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sv.edu.udb.data_collector.controller.request.InviteMemberRequest;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.*;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.repository.WorkspaceMemberRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.service.MembershipService;
import sv.edu.udb.data_collector.service.mapper.MemberMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipServiceImpl implements MembershipService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final MemberMapper mapper;

    @Override
    @Transactional
    public MemberResponse invite(String workspaceId, InviteMemberRequest request) {
        var workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (memberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a member");
        }

        var role = request.getRole() != null ? request.getRole() : MemberRole.MEMBER;

        var member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .status(MemberStatus.ACTIVE) // si quieres flujo de invitación pendiente, usa INVITED
                .build();

        member = memberRepository.save(member);
        return mapper.toResponse(member);
    }

    @Override
    public List<MemberResponse> list(String workspaceId) {
        // Valido que el workspace exista (y si no, 404)
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        return memberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
