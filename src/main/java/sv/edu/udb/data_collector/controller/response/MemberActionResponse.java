package sv.edu.udb.data_collector.controller.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.udb.data_collector.domain.MemberRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberActionResponse {
    private String action; // e.g. "ROLE_UPDATED", "MEMBER_REMOVED"
    private String workspaceId;
    private String userId;
    private MemberRole newRole; // solo para updates
    private Instant timestamp;
}
