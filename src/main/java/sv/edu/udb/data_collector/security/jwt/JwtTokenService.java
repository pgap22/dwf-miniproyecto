package sv.edu.udb.data_collector.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sv.edu.udb.data_collector.security.token.TokenPayload;
import sv.edu.udb.data_collector.security.token.TokenService;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service("jwtTokenService")
public class JwtTokenService implements TokenService {

    @Value("${security.token.jwt.secret}")
    private String secret;

    @Value("${security.token.jwt.expiration-minutes:60}")
    private long expirationMinutes;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public String generateAccessToken(String userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(expirationMinutes));
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("email", email)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public TokenPayload parse(String token) {
        var jws = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
        var c = jws.getBody();
        return new TokenPayload(c.getSubject(), c.get("email", String.class), c.getExpiration().toInstant());
    }

    @Override
    public boolean isValid(String token) {
        try { parse(token); return true; } catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
