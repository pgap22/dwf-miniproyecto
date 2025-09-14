package sv.edu.udb.data_collector.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sv.edu.udb.data_collector.security.token.TokenPayload;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    // ---------- Constantes GIVEN ----------
    private static final String GIVEN_SUBJECT = "u1";
    private static final String GIVEN_EMAIL   = "ana@p.com";
    private static final String SECRET_B64    = "dGhpc19pc19hX2xvbmdfc2VjcmV0X2tleV9iYXNlNjQ="; // demo
    private static final long   EXP_MINUTES   = 60L;

    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        // ---------- Arrange (común) ----------
        service = new JwtTokenService();
        ReflectionTestUtils.setField(service, "secret", SECRET_B64);
        ReflectionTestUtils.setField(service, "expirationMinutes", EXP_MINUTES);
    }

    @Test
    @DisplayName("generate + parse: token válido y payload correcto")
    void genera_y_parsea_valido() {
        // ---------- Arrange ----------
        String expectedSubject = GIVEN_SUBJECT;
        String expectedEmail   = GIVEN_EMAIL;

        // ---------- Act ----------
        String token = service.generateAccessToken(GIVEN_SUBJECT, GIVEN_EMAIL); // <- breakpoint útil
        TokenPayload payload = service.parse(token);                             // <- breakpoint útil
        boolean valid = service.isValid(token);

        // ---------- Assert ----------
        assertThat(token).isNotBlank();
        assertThat(payload.subject()).isEqualTo(expectedSubject);
        assertThat(payload.email()).isEqualTo(expectedEmail);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isValid: token malformado es inválido")
    void token_malformado_es_invalido() {
        // ---------- Arrange ----------
        String malformed = "abc.def.ghi";

        // ---------- Act ----------
        boolean valid = service.isValid(malformed); // <- breakpoint útil

        // ---------- Assert ----------
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("isValid: token expirado es inválido")
    void token_expirado_invalido() {
        // ---------- Arrange ----------
        // Forzamos expiración en el pasado para el token que generaremos.
        ReflectionTestUtils.setField(service, "expirationMinutes", -1L);

        // ---------- Act ----------
        String expiredToken = service.generateAccessToken(GIVEN_SUBJECT, GIVEN_EMAIL); // <- breakpoint útil
        boolean valid = service.isValid(expiredToken);                                 // <- breakpoint útil

        // ---------- Assert ----------
        assertThat(valid).isFalse();
    }
}
