package sv.edu.udb.data_collector.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public UserResponse create(@Valid @RequestBody UserRequest req) {
    return userService.create(req);
  }

  @GetMapping("/{id}")
  public UserResponse findById(@PathVariable String id) {
    return userService.findById(id);
  }

  @GetMapping
  public List<UserResponse> list() {
    return userService.list();
  }
}