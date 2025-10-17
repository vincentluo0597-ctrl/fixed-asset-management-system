package com.example.gdzc.controller;

import com.example.gdzc.domain.User;
import com.example.gdzc.dto.CreateUserRequest;
import com.example.gdzc.dto.UpdateUserRequest;
import com.example.gdzc.dto.UserDto;
import com.example.gdzc.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> list() {
        List<UserDto> result = userService.list().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<UserDto>> page(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> dtoPage = userService.page(pageable).map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest req) {
        User created = userService.create(req);
        return ResponseEntity.ok(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserRequest req) {
        User updated = userService.update(id, req);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(User u) {
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .displayName(u.getDisplayName())
                .roles(u.getRoles())
                .createdAt(u.getCreatedAt())
                .build();
    }
}