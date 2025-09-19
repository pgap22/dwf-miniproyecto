package sv.edu.udb.data_collector.controller.response;

import lombok.Builder;
import lombok.Value;
import sv.edu.udb.data_collector.domain.MemberRole;

import java.time.Instant;

@Value
@Builder
public class MemberResponse {
    String id;
    String userId;
    String email;
    String name;
    MemberRole role;
    Instant joinedAt;
}
