package com.talentacquisition.applicationservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Candidate endpoints
                .requestMatchers(HttpMethod.POST, "/api/applications").hasAnyRole("CANDIDATE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/applications/my").hasAnyRole("CANDIDATE", "ADMIN")

                // HR / Admin endpoints
                .requestMatchers(HttpMethod.GET, "/api/applications/job/**").hasAnyRole("HR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/applications/*/status").hasAnyRole("HR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/applications/*/shortlist").hasAnyRole("HR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/applications/*/reject").hasAnyRole("HR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/applications/*/select").hasAnyRole("HR", "ADMIN")

                // View specific application
                .requestMatchers(HttpMethod.GET, "/api/applications/*").authenticated()

                // Any other request must be authenticated
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
