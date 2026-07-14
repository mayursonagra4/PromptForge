package com.mayur.distributed_promptforge.api_gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RouteConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return Mono.just(xForwardedFor.split(",")[0].trim());
            }
            return Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "127.0.0.1"
            );
        };
    }

    @Bean("authRateLimiter")
    @Primary
    public RedisRateLimiter authRateLimiter() {
        // replenishRate: 5 requests per second
        // burstCapacity: 10 requests burst
        return new RedisRateLimiter(5, 10);
    }

    @Bean("webhookRateLimiter")
    public RedisRateLimiter webhookRateLimiter() {
        // replenishRate: 5 requests per second
        // burstCapacity: 10 requests burst
        return new RedisRateLimiter(5, 10);
    }

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            KeyResolver ipKeyResolver,
            @Qualifier("authRateLimiter") RedisRateLimiter authRateLimiter,
            @Qualifier("webhookRateLimiter") RedisRateLimiter webhookRateLimiter) {
        return builder.routes()
                .route("account-auth-route", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(authRateLimiter);
                                    c.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://ACCOUNT-SERVICE"))
                .route("account-billing-route-me", r -> r
                        .path("/api/me/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ACCOUNT-SERVICE"))
                .route("account-billing-route-payments", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ACCOUNT-SERVICE"))
                .route("account-billing-route-plans", r -> r
                        .path("/api/plans", "/api/plans/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ACCOUNT-SERVICE"))
                .route("account-admin-route", r -> r
                        .path("/api/admin/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://ACCOUNT-SERVICE"))
                .route("workspace-route", r -> r
                        .path("/api/projects/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://WORKSPACE-SERVICE"))
                .route("intelligence-route", r -> r
                        .path("/api/chat/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                        .uri("lb://INTELLIGENCE-SERVICE"))
                .route("account-billing-route-webhooks", r -> r
                        .path("/webhooks/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(webhookRateLimiter);
                                    c.setKeyResolver(ipKeyResolver);
                                })
                        )
                        .uri("lb://ACCOUNT-SERVICE"))
                .build();
    }
}

