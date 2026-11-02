package com.library.digital_library.unittest;

import com.library.digital_library.dto.AuthRequest;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void publicRegistrationAlwaysCreatesUserRoleAndHashesPassword() {
        AuthRequest request = new AuthRequest();
        request.setUsername(" reader ");
        request.setEmail("reader@example.test");
        request.setPassword("secret");
        request.setRole("ADMIN");
        when(userRepository.existsByUsername("reader")).thenReturn(false);
        when(userRepository.existsByEmail("reader@example.test")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.registerPublicUser(request);

        assertEquals("reader", registered.getUsername());
        assertEquals("USER", registered.getRole());
        assertEquals("encoded", registered.getPassword());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void adminCannotAssignUnsupportedRole() {
        AuthRequest request = new AuthRequest();
        request.setUsername("reader");
        request.setEmail("reader@example.test");
        request.setPassword("secret");
        request.setRole("SUPERUSER");
        when(userRepository.existsByUsername("reader")).thenReturn(false);
        when(userRepository.existsByEmail("reader@example.test")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.createUserByAdmin(request));
    }
}
