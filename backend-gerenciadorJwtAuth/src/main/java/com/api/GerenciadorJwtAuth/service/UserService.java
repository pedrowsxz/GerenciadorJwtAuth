package com.api.GerenciadorJwtAuth.service;

import com.api.GerenciadorJwtAuth.dto.UserCreateDTO;
import com.api.GerenciadorJwtAuth.dto.UserDTO;
import com.api.GerenciadorJwtAuth.exception.ResourceNotFoundException;
import com.api.GerenciadorJwtAuth.model.Role;
import com.api.GerenciadorJwtAuth.model.User;
import com.api.GerenciadorJwtAuth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        // Set default role if not provided
        if (userCreateDTO.getRole() == null) {
            userCreateDTO.setRole(Role.USER);
        }

        User user = User.builder()
                .name(userCreateDTO.getName())
                .cpf(userCreateDTO.getCpf())
                .password(passwordEncoder.encode(userCreateDTO.getPassword()))
                .role(userCreateDTO.getRole())
                .build();

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    public UserDTO updateUser(Long id, UserCreateDTO userCreateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setName(userCreateDTO.getName());
        user.setCpf(userCreateDTO.getCpf());

        // Only update password if provided
        if (userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        }

        if (userCreateDTO.getRole() != null) {
            user.setRole(userCreateDTO.getRole());
        }

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .cpf(user.getCpf())
                .role(user.getRole())
                .build();
    }
}