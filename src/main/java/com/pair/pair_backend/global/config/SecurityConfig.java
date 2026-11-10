package com.pair.pair_backend.global.config;

import com.pair.pair_backend.global.security.CurrentUserJwtAuthenticationConverter;
import com.pair.pair_backend.global.exception.ErrorCode;
import com.pair.pair_backend.global.response.ApiResponse;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeError(response, objectMapper, ErrorCode.UNAUTHORIZED);
    }

    @Bean
    AccessDeniedHandler apiAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeError(response, objectMapper, ErrorCode.FORBIDDEN);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CurrentUserJwtAuthenticationConverter currentUserConverter,
            CorsConfigurationSource corsConfigurationSource,
            AuthenticationEntryPoint apiAuthenticationEntryPoint,
            AccessDeniedHandler apiAccessDeniedHandler
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(currentUserConverter)))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/signup/personal",
                                "/api/v1/auth/signup/company",
                                "/api/v1/auth/company-verifications",
                                "/api/v1/auth/phone-verifications",
                                "/api/v1/auth/phone-verifications/*/confirm"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/jobs",
                                "/api/v1/jobs/*",
                                "/api/v1/metadata/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/companies/me",
                                "/api/v1/companies/me/job/publication"
                        ).hasAuthority("COMPANY")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/users/me/profile",
                                "/api/v1/users/me/account"
                        ).hasAnyAuthority("PERSONAL", "COMPANY")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/notifications/*/read"
                        ).hasAuthority("PERSONAL")

                        .requestMatchers(
                                "/api/v1/companies/me",
                                "/api/v1/companies/me/**"
                        ).hasAuthority("COMPANY")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users/me/bookmarks",
                                "/api/v1/users/me/applications",
                                "/api/v1/resumes/me"
                        ).hasAuthority("PERSONAL")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/users/me/job-roles",
                                "/api/v1/users/me/skills",
                                "/api/v1/resumes/me",
                                "/api/v1/companies/*/bookmark",
                                "/api/v1/applications/*/interviews/*/attendance"
                        ).hasAuthority("PERSONAL")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/companies/*/bookmark"
                        ).hasAuthority("PERSONAL")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/jobs/*/applications"
                        ).hasAuthority("PERSONAL")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:3000}") List<String> allowedOrigins
    ) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
        config.setExposedHeaders(List.of("Retry-After"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/v1/**", config);
        return source;
    }

    private String requestId() {
        String requestId = MDC.get("request_id");
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }

    private void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            ErrorCode errorCode
    ) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(
                errorCode, Map.of(), requestId()));
    }


}
