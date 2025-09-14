package sv.edu.udb.data_collector.security.jwt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import sv.edu.udb.data_collector.security.token.TokenPayload;
import sv.edu.udb.data_collector.security.token.TokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    // ------- GIVEN / EXPECTED -------
    private static final String BEARER = "Bearer ";
    private static final String GIVEN_TOKEN = "t";
    private static final String GIVEN_SUBJECT = "u1";
    private static final String GIVEN_EMAIL = "ana@p.com";

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Con token válido: setea Authentication en el SecurityContext")
    void con_token_valido_setea_authentication() throws Exception {
        // ---------- Arrange ----------
        TokenService tokenService = mock(TokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);

        when(tokenService.isValid(GIVEN_TOKEN)).thenReturn(true);
        when(tokenService.parse(GIVEN_TOKEN)).thenReturn(new TokenPayload(GIVEN_SUBJECT, GIVEN_EMAIL, null));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER + GIVEN_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // ---------- Act ----------
        filter.doFilter(request, response, chain); // <- breakpoint útil

        // ---------- Assert ----------
        var auth = SecurityContextHolder.getContext().getAuthentication(); // <- breakpoint útil
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(GIVEN_EMAIL);

        verify(tokenService).isValid(GIVEN_TOKEN);
        verify(tokenService).parse(GIVEN_TOKEN);
        verifyNoMoreInteractions(tokenService);
    }

    @Test
    @DisplayName("Sin header Authorization: no autentica ni invoca TokenService")
    void sin_header_no_autentica() throws Exception {
        // ---------- Arrange ----------
        TokenService tokenService = mock(TokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // ---------- Act ----------
        filter.doFilter(request, response, chain); // <- breakpoint útil

        // ---------- Assert ----------
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Header presente pero token inválido: no autentica")
    void header_presente_token_invalido_no_autentica() throws Exception {
        // ---------- Arrange ----------
        TokenService tokenService = mock(TokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);

        when(tokenService.isValid(GIVEN_TOKEN)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", BEARER + GIVEN_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // ---------- Act ----------
        filter.doFilter(request, response, chain);

        // ---------- Assert ----------
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService).isValid(GIVEN_TOKEN);
        verifyNoMoreInteractions(tokenService);
    }
}
