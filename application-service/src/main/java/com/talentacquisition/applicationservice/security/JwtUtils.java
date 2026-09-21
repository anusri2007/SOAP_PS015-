package com.talentacquisition.applicationservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            userIdObj = claims.get("candidateId");
        }
        if (userIdObj == null) {
            userIdObj = claims.get("id");
        }
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = parseToken(token);
        List<String> rawRoles = new ArrayList<>();

        Object rolesObj = claims.get("roles");
        if (rolesObj == null) {
            rolesObj = claims.get("role");
        }
        if (rolesObj == null) {
            rolesObj = claims.get("authorities");
        }

        if (rolesObj instanceof List<?>) {
            for (Object item : (List<?>) rolesObj) {
                if (item != null) {
                    rawRoles.add(item.toString());
                }
            }
        } else if (rolesObj instanceof String) {
            String[] split = ((String) rolesObj).split(",");
            for (String r : split) {
                rawRoles.add(r.trim());
            }
        }

        return rawRoles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

