package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.MemberRole;

import java.util.List;

public interface WorkspaceUserService {

    MemberResponse invite(String workspaceId, String email);

    List<MemberResponse> list(String workspaceId);

    boolean changeMemberRole(String workspaceId, String userId, MemberRole newRole);

    boolean removeMember(String workspaceId, String userId);
}
