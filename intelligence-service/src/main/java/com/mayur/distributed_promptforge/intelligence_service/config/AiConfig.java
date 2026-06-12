package com.mayur.distributed_promptforge.intelligence_service.config;

import com.mayur.distributed_promptforge.intelligence_service.llm.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(new TokenUsageAuditAdvisor())
                .build();
    }
}
