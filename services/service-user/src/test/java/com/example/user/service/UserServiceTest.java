package com.example.user.service;

import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import com.example.user.dto.UserRequest;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void create_WhenEmailExists_ShouldThrowException() {
        UserRequest request = new UserRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_WhenValid_ShouldSaveUser() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        User savedUser = User.builder().id(1L).name("John Doe").email("john@example.com").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findById_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(1L));
    }
}
