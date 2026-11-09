package com.library.digital_library.unittest;

import com.library.digital_library.security.CustomUserDetailsService;
import com.library.digital_library.security.JwtAuthenticationFilter;
import com.library.digital_library.security.JwtUtils;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesCurrentAuthoritiesFromUserDetails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) -> {};
        UserDetails user = User.withUsername("admin")
                .password("encoded-password")
                .authorities("ROLE_ADMIN")
                .build();

        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);

        filter.doFilter(request, response, chain);

        assertEquals(
                Collections.singleton("ROLE_ADMIN"),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()));
        verify(userDetailsService).loadUserByUsername("admin");
    }

    @Test
    void ignoresStaleBearerTokenForPublicRegistration() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] chainCalled = {false};
        FilterChain chain = (servletRequest, servletResponse) -> chainCalled[0] = true;

        filter.doFilter(request, response, chain);

        assertTrue(chainCalled[0]);
        assertEquals(200, response.getStatus());
        verifyNoInteractions(jwtUtils, userDetailsService);
    }
}
