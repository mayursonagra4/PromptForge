package com.mayur.distributed_promptforge.common_lib.security;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@AutoConfiguration
@ComponentScan("com.mayur.distributed_promptforge.common_lib")
public class SharedSecurityAutoConfiguration {

    @Value("${app.security.internal-secret:}")
    private String internalSecret;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (internalSecret != null && !internalSecret.isBlank()) {
                requestTemplate.header("X-Internal-Secret", internalSecret);
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String token) {
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
