package com.mayur.distributed_promptforge.account_service.config;

import com.mayur.distributed_promptforge.account_service.entity.User;
import com.mayur.distributed_promptforge.account_service.entity.UserRole;
import com.mayur.distributed_promptforge.account_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:mayursonagra4@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:Mayur@2006}")
    private String adminPassword;

    @Bean
    public CommandLineRunner ensureAdminUser() {
        return args -> userRepository.findByUsernameIgnoreCase(adminEmail)
                .ifPresentOrElse(user -> {
                    if (user.getRole() != UserRole.ADMIN) {
                        user.setRole(UserRole.ADMIN);
                        userRepository.save(user);
                    }
                }, () -> {
                    User admin = User.builder()
                            .username(adminEmail)
                            .name("System Admin")
                            .password(passwordEncoder.encode(adminPassword))
                            .role(UserRole.ADMIN)
                            .blocked(false)
                            .emailVerified(true)
                            .build();
                    userRepository.save(admin);
                });
    }
}
