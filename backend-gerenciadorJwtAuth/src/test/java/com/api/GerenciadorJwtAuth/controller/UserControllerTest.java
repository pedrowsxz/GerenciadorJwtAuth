package com.api.GerenciadorJwtAuth.controller;

import com.api.GerenciadorJwtAuth.dto.UserCreateDTO;
import com.api.GerenciadorJwtAuth.dto.UserDTO;
import com.api.GerenciadorJwtAuth.exception.ResourceNotFoundException;
import com.api.GerenciadorJwtAuth.model.Role;
import com.api.GerenciadorJwtAuth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO userDTO1;
    private UserDTO userDTO2;
    private UserCreateDTO userCreateDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        userDTO1 = UserDTO.builder()
                .id(1L)
                .name("Test User")
                .cpf("123.456.789-01")
                .role(Role.USER)
                .build();

        userDTO2 = UserDTO.builder()
                .id(2L)
                .name("Admin User")
                .cpf("987.654.321-09")
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
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        List<UserDTO> users = Arrays.asList(userDTO1, userDTO2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test User")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Admin User")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithValidId_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO1);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithValidData_ShouldReturnCreatedUser() throws Exception {
        UserDTO createdUserDTO = UserDTO.builder()
                .id(3L)
                .name("New User")
                .cpf("111.222.333-44")
                .role(Role.USER)
                .build();

        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(createdUserDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New User")));

        verify(userService, times(1)).createUser(any(UserCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithValidId_ShouldReturnUpdatedUser() throws Exception {
        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .name("Updated Name")
                .cpf("123.456.789-01")
                .role(Role.ADMIN)
                .build();

        UserCreateDTO updateDTO = UserCreateDTO.builder()
                .name("Updated Name")
                .cpf("123.456.789-01")
                .password("newpassword")
                .role(Role.ADMIN)
                .build();

        when(userService.updateUser(eq(1L), any(UserCreateDTO.class))).thenReturn(updatedUserDTO);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.role", is("ADMIN")));

        verify(userService, times(1)).updateUser(eq(1L), any(UserCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WithInvalidId_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(999L);

        mockMvc.perform(delete("/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(999L);
    }

    // Test for unauthorized access if required
    @Test
    void getAllUsers_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }
}