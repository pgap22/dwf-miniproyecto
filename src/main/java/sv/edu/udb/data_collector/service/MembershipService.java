package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.InviteMemberRequest;
import sv.edu.udb.data_collector.controller.response.MemberResponse;

import java.util.List;

public interface MembershipService {

    MemberResponse invite(String workspaceId, InviteMemberRequest request);

    List<MemberResponse> list(String workspaceId);
}
