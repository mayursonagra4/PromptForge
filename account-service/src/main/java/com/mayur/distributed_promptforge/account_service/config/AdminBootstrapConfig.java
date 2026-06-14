package com.mayur.distributed_promptforge.account_service.config;

import com.mayur.distributed_promptforge.account_service.entity.Plan;
import com.mayur.distributed_promptforge.account_service.entity.User;
import com.mayur.distributed_promptforge.account_service.entity.UserRole;
import com.mayur.distributed_promptforge.account_service.repository.PlanRepository;
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
    private final PlanRepository planRepository;

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

    @Bean
    public CommandLineRunner ensureDefaultPlan() {
        return args -> {
            if (planRepository.findByNameIgnoreCase("FREE").isEmpty()) {
                Plan freePlan = new Plan();
                freePlan.setName("FREE");
                freePlan.setPriceInPaise(0L);
                freePlan.setMaxProjects(5);
                freePlan.setMaxTokensPerDay(100);
                freePlan.setUnlimitedAi(false);
                freePlan.setValidityDays(36500); // 100 years
                freePlan.setActive(true);
                planRepository.save(freePlan);
            }
        };
    }
}
