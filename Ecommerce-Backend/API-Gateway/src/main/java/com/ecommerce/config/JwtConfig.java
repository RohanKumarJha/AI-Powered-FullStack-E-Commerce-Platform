package com.ecommerce.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;


    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        byte[] decodedSecret =
                Base64.getDecoder().decode(jwtSecret);
        if (decodedSecret.length != 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain exactly 32 decoded bytes"
            );
        }
        SecretKey secretKey =
                new SecretKeySpec(decodedSecret, "HmacSHA256");
        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

}