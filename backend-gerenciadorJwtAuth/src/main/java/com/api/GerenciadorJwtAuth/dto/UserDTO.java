package com.api.GerenciadorJwtAuth.dto;

import com.api.GerenciadorJwtAuth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String cpf;
    private Role role;
}