package com.demo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        // Rate limit by the user header set in JwtAuthenticationFilter
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-Auth-User"))
                .defaultIfEmpty("anonymous");
    }
}
