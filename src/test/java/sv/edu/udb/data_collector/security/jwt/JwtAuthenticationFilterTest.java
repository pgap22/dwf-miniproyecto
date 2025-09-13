package sv.edu.udb.data_collector.security.jwt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import sv.edu.udb.data_collector.security.token.TokenPayload;
import sv.edu.udb.data_collector.security.token.TokenService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    @Test
    void con_token_valido_setea_authentication() throws Exception {
        TokenService tokenService = mock(TokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);

        when(tokenService.isValid("t")).thenReturn(true);
        when(tokenService.parse("t")).thenReturn(new TokenPayload("u1", "ana@p.com", null));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer t");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("ana@p.com");
    }

    @Test
    void sin_header_no_autentica() throws Exception {
        TokenService tokenService = mock(TokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenService);
    }
}
