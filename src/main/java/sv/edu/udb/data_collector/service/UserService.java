package sv.edu.udb.data_collector.service;

import java.util.List;

import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;

public interface UserService {
  UserResponse create(UserRequest req);
  //Los get me gustan mas findById
  UserResponse findById(String id);
  List<UserResponse> list();
}
