package sv.edu.udb.data_collector.service;

import sv.edu.udb.data_collector.controller.request.LoginRequest;
import sv.edu.udb.data_collector.controller.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
