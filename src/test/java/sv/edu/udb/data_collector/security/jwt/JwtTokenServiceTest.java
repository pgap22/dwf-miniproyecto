package sv.edu.udb.data_collector.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sv.edu.udb.data_collector.security.token.TokenPayload;

import static org.assertj.core.api.Assertions.*;

class JwtTokenServiceTest {

    JwtTokenService service;

    @BeforeEach
    void setUp() {
        service = new JwtTokenService();
        // Clave Base64 (ejemplo). Sustituye por una segura en producción.
        ReflectionTestUtils.setField(service, "secret", "dGhpc19pc19hX2xvbmdfc2VjcmV0X2tleV9iYXNlNjQ=");
        ReflectionTestUtils.setField(service, "expirationMinutes", 60L);
    }

    @Test
    void genera_y_parsea_valido() {
        String token = service.generateAccessToken("u1", "ana@p.com");
        assertThat(token).isNotBlank();

        TokenPayload p = service.parse(token);
        assertThat(p.subject()).isEqualTo("u1");
        assertThat(p.email()).isEqualTo("ana@p.com");
        assertThat(service.isValid(token)).isTrue();
    }

    @Test
    void token_malformado_es_invalido() {
        assertThat(service.isValid("abc.def.ghi")).isFalse();
    }

    @Test
    void token_expirado_invalido() {
        ReflectionTestUtils.setField(service, "expirationMinutes", -1L); // pasado
        String token = service.generateAccessToken("u1", "ana@p.com");
        assertThat(service.isValid(token)).isFalse();
    }
}
