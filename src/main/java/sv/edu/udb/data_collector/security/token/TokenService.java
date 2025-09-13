package sv.edu.udb.data_collector.security.token;

public interface TokenService {
    String generateAccessToken(String userId, String email);
    TokenPayload parse(String token);
    boolean isValid(String token);
}
