package sv.edu.udb.data_collector.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.MemberRole;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.domain.WorkspaceUser;
import sv.edu.udb.data_collector.repository.UserRepository;
import sv.edu.udb.data_collector.repository.WorkspaceRepository;
import sv.edu.udb.data_collector.repository.WorkspaceUserRepository;
import sv.edu.udb.data_collector.service.WorkspaceUserService;
import sv.edu.udb.data_collector.service.mapper.WorkspaceUserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceUserServiceImpl implements WorkspaceUserService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceUserRepository memberRepository;
    private final WorkspaceUserMapper mapper;

    @Override
    @Transactional
    public MemberResponse invite(String workspaceId, String email) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        WorkspaceUser member = WorkspaceUser.builder()
                .workspace(ws)
                .user(user)
                .role(MemberRole.MEMBER)
                .build();

        try {
            WorkspaceUser saved = memberRepository.save(member);
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // UNIQUE (workspaceId, userId)
            // Tu RestExceptionHandler ya mapea DataAccessException -> 409
            throw ex;
        }
    }

    @Override
    public List<MemberResponse> list(String workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        return memberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public boolean changeMemberRole(String workspaceId, String userId, MemberRole newRole) {
        int updated = memberRepository.updateRoleByWorkspaceIdAndUserId(workspaceId, userId, newRole);
        if (updated == 0) throw new EntityNotFoundException("Member not found");
        return true;
    }

    @Override
    @Transactional
    public boolean removeMember(String workspaceId, String userId) {
        long deleted = memberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        if (deleted == 0) throw new EntityNotFoundException("Member not found");
        return true;
    }
}
