package sv.edu.udb.data_collector.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", source = "password")
  User toUser(UserRequest req);

  UserResponse toUserResponse(User user);
}