package com.talentacquisition.applicationservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);
                Long userId = jwtUtils.getUserIdFromToken(jwt);
                List<GrantedAuthority> authorities = jwtUtils.getAuthoritiesFromToken(jwt);

                UserPrincipal principal = UserPrincipal.builder()
                        .userId(userId)
                        .username(username)
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Fallback for gateway header propagation (when gateway authenticates JWT)
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader == null) {
                    userIdHeader = request.getHeader("X-Candidate-Id");
                }
                String rolesHeader = request.getHeader("X-User-Roles");

                if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(rolesHeader)) {
                    Long userId = null;
                    try {
                        userId = Long.parseLong(userIdHeader);
                    } catch (NumberFormatException ignored) {
                    }

                    List<GrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                            .map(String::trim)
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UserPrincipal principal = UserPrincipal.builder()
                            .userId(userId)
                            .username("user-" + userId)
                            .authorities(authorities)
                            .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}

