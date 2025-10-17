package com.example.gdzc.service;

import com.example.gdzc.domain.User;
import com.example.gdzc.domain.UserRole;
import com.example.gdzc.dto.CreateUserRequest;
import com.example.gdzc.dto.UpdateUserRequest;
import com.example.gdzc.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> list() {
        return userRepository.findAll();
    }

    public Page<User> page(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User create(CreateUserRequest req) {
        String username = req.getUsername() != null ? req.getUsername().trim() : null;
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        Set<UserRole> roles = req.getRoles() != null ? new HashSet<>(req.getRoles()) : new HashSet<>();
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个角色");
        }
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .displayName(req.getDisplayName())
                .roles(roles)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (req.getDisplayName() != null) {
            user.setDisplayName(req.getDisplayName());
        }
        if (StringUtils.hasText(req.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getRoles() != null) {
            Set<UserRole> roles = new HashSet<>(req.getRoles());
            if (roles.isEmpty()) {
                throw new IllegalArgumentException("至少需要一个角色");
            }
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("用户不存在");
        }
        userRepository.deleteById(id);
    }
}