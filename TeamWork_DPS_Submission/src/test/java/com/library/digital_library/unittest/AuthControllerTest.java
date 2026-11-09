package com.library.digital_library.unittest;

import com.library.digital_library.controller.AuthController;
import com.library.digital_library.dto.AuthRequest;
import com.library.digital_library.dto.AuthResponse;
import com.library.digital_library.dto.LoginRequest;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.security.JwtUtils;
import com.library.digital_library.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsSignedTokenAndRole() {
        LoginRequest request = new LoginRequest("admin", "secret");
        User user = new User();
        user.setUsername("admin");
        user.setRole("ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtUtils.generateJwtToken("admin", "ADMIN")).thenReturn("signed-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = assertInstanceOf(AuthResponse.class, response.getBody());
        assertEquals("ADMIN", body.getRole());
        assertEquals("signed-token", body.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginRejectsInvalidCredentials() {
        LoginRequest request = new LoginRequest("admin", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
