package com.mayur.distributed_promptforge.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.mayur.distributed_promptforge.api_gateway.config.SecurityProperties;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
