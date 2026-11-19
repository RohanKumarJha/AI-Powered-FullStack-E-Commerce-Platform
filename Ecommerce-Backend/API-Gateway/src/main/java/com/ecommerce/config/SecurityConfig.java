
package com.ecommerce.config;

import com.ecommerce.exception.JwtAccessDeniedHandler;
import com.ecommerce.exception.JwtAuthenticationEntryPoint;
import com.ecommerce.security.JwtAuthenticationConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler) {

        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges

                        // Public endpoints
                        .pathMatchers(
                                "/api/v1/users/register",
                                "/api/v1/auth/**",
                                "/actuator/health"
                        ).permitAll()

                        // Admin-only endpoints
                        .pathMatchers("/api/v1/roles/**")
                        .hasRole("ADMIN")

                        // Identity user and address endpoints
                        .pathMatchers(
                                "/api/v1/users/**",
                                "/api/v1/addresses/**"
                        ).hasAnyRole("ADMIN", "CUSTOMER")

                        // All remaining endpoints require authentication
                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2

                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )

                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .build();
    }
}
