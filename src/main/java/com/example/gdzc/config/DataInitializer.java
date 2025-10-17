package com.example.gdzc.config;

import com.example.gdzc.domain.User;
import com.example.gdzc.domain.UserRole;
import com.example.gdzc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 创建默认管理员
        String username = "admin";
        if (userRepository.findByUsername(username).isEmpty()) {
            User admin = User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .displayName("系统管理员")
                    .roles(Set.of(UserRole.SUPER_ADMIN))
                    .build();
            userRepository.save(admin);
            System.out.println("[DataInitializer] 默认管理员已创建: admin / Admin@123");
        }
    }
}