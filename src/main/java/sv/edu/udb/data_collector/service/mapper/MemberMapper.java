package sv.edu.udb.data_collector.service.mapper;

import org.springframework.stereotype.Component;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.WorkspaceMember;

@Component
public class MemberMapper {

    public MemberResponse toResponse(WorkspaceMember m) {
        if (m == null) return null;
        return MemberResponse.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .email(m.getUser().getEmail())
                .name(m.getUser().getName())
                .role(m.getRole().name())
                .status(m.getStatus().name())
                .joinedAt(m.getCreatedAt())
                .build();
    }
}
