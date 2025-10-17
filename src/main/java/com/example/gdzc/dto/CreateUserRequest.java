package com.example.gdzc.dto;

import com.example.gdzc.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String displayName;
    @NotNull(message = "角色不能为空")
    @NotEmpty(message = "至少需要一个角色")
    private Set<UserRole> roles;
}