package com.ecommerce.productservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtUtil {

    private final String SECRET = "my-secret-key-my-secret-key-my-secret-key";

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRoles(String token) {
        return extractClaims(token).get("roles", String.class);
    }
}
