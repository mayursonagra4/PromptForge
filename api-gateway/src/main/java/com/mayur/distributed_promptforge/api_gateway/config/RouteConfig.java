package com.mayur.distributed_promptforge.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-auth-route", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
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
                        .uri("lb://ACCOUNT-SERVICE"))
                .build();
    }
}
