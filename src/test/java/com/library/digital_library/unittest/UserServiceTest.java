package com.library.digital_library.unittest;

import com.library.digital_library.dto.UserUpdateRequest;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("reader");
        user.setEmail("reader@example.test");
        user.setPassword("old-hash");
        user.setRole("USER");
    }

    @Test
    void updateUserChangesAccountAndHashesOptionalPassword() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername(" updated-reader ");
        request.setEmail(" updated@example.test ");
        request.setPassword("new-password");
        request.setRole("admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("updated-reader", 1L)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("updated@example.test", 1L)).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, request);

        assertEquals("updated-reader", updated.getUsername());
        assertEquals("updated@example.test", updated.getEmail());
        assertEquals("ADMIN", updated.getRole());
        assertEquals("new-hash", updated.getPassword());
        verify(passwordEncoder).encode("new-password");
    }

    @Test
    void deleteUserRejectsTheCurrentlySignedInAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> userService.deleteUser(1L, "reader"));

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserRemovesAnotherAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, "admin");

        verify(userRepository).delete(user);
    }
}
