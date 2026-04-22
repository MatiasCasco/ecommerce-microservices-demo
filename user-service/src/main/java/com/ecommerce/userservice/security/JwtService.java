package com.ecommerce.userservice.security;

import com.ecommerce.common.security.JwtUtil;
import com.ecommerce.userservice.domain.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;

    public String generateToken(String email, Role role) {
          return jwtUtil.generateToken(email, role.name());
    }
}
