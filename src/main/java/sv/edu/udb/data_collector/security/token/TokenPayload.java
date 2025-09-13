package sv.edu.udb.data_collector.security.token;

import java.time.Instant;

public record TokenPayload(String subject, String email, Instant expiresAt) {}
