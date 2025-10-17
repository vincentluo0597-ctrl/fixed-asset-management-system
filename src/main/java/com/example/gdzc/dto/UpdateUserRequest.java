package com.example.gdzc.dto;

import com.example.gdzc.domain.UserRole;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String displayName;
    private String password; // 可选，若设置则更新
    private Set<UserRole> roles; // 可选，若设置则更新
}