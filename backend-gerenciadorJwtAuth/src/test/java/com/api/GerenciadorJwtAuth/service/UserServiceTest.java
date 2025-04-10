package com.api.GerenciadorJwtAuth.service;

import com.api.GerenciadorJwtAuth.dto.UserCreateDTO;
import com.api.GerenciadorJwtAuth.dto.UserDTO;
import com.api.GerenciadorJwtAuth.exception.ResourceNotFoundException;
import com.api.GerenciadorJwtAuth.model.Role;
import com.api.GerenciadorJwtAuth.model.User;
import com.api.GerenciadorJwtAuth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private UserCreateDTO userCreateDTO;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("Test User")
                .cpf("123.456.789-01")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        user2 = User.builder()
                .id(2L)
                .name("Admin User")
                .cpf("987.654.321-09")
                .password("encoded_password")
                .role(Role.ADMIN)
                .build();

        userCreateDTO = UserCreateDTO.builder()
                .name("New User")
                .cpf("111.222.333-44")
                .password("password123")
                .role(Role.USER)
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getName()).isEqualTo("Test User");
        assertThat(result.get(1).getName()).isEqualTo("Admin User");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        UserDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getCpf()).isEqualTo("123.456.789-01");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void createUser_WithValidData_ShouldCreateAndReturnUser() {
        // Given
        User newUser = User.builder()
                .id(3L)
                .name("New User")
                .cpf("111.222.333-44")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        UserDTO result = userService.createUser(userCreateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getCpf()).isEqualTo("111.222.333-44");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithNullRole_ShouldAssignDefaultRoleUser() {
        // Given
        userCreateDTO.setRole(null);

        User savedUser = User.builder()
                .id(3L)
                .name("New User")
                .cpf("111.222.333-44")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDTO result = userService.createUser(userCreateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithValidIdAndData_ShouldUpdateAndReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserCreateDTO updateDTO = UserCreateDTO.builder()
                .name("Updated Name")
                .cpf("123.456.789-01")
                .role(Role.ADMIN)
                .build();

        // When
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithPassword_ShouldEncodePassword() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);
        when(passwordEncoder.encode("new_password")).thenReturn("new_encoded_password");

        UserCreateDTO updateDTO = UserCreateDTO.builder()
                .name("Updated Name")
                .cpf("123.456.789-01")
                .password("new_password")
                .role(Role.USER)
                .build();

        // When
        userService.updateUser(1L, updateDTO);

        // Then
        verify(passwordEncoder, times(1)).encode("new_password");
    }

    @Test
    void updateUser_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(999L, userCreateDTO);
        });
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
