package com.library.digital_library.unittest;

import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.security.CustomUserDetailsService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ROLE_ADMIN", "admin"})
    void normalizesAdminRole(String role) {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(role);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }
}
