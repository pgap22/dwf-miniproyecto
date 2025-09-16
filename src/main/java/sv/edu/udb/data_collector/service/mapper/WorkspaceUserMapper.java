package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.WorkspaceUser;

@Mapper(componentModel = "spring")
public interface WorkspaceUserMapper {

    @Mapping(source = "user.id",    target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.name",  target = "name")
    @Mapping(source = "role",       target = "role")       // enum->String se hace solo
    @Mapping(source = "createdAt",  target = "joinedAt")
    MemberResponse toResponse(WorkspaceUser m);
}
