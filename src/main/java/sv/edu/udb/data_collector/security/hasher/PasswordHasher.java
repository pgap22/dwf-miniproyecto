package sv.edu.udb.data_collector.security.hasher;

public interface PasswordHasher {
    String hash(String raw);
    boolean matches(String raw, String hash);
}