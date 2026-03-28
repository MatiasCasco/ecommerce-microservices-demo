package com.ecommerce.userservice.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.util.CommerceLog;
import com.ecommerce.userservice.domain.entity.User;
import com.ecommerce.userservice.domain.enums.UserErrorCode;
import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.request.RegisterRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    // Inyeccion con lombok
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        LOGGER.info("{}",
                CommerceLog.info(
                        "AUTH",
                        "REGISTER_ATTEMPT",
                        "User trying to register",
                        null,
                        Map.of("email", request.getEmail())
                )
        );

        // validar duplicado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered", UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //  usar factory method
        User user = User.create(request.getEmail(), encodedPassword);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        LOGGER.info("{}",
                CommerceLog.info(
                        "AUTH",
                        "REGISTER_SUCCESS",
                        "User registered successfully",
                        null,
                        Map.of("email", user.getEmail())
                )
        );

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {

        LOGGER.info("{}",
                CommerceLog.info(
                        "AUTH",
                        "LOGIN_ATTEMPT",
                        "User trying to login",
                        null,
                        Map.of("email", request.getEmail())
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        LOGGER.info("{}",
                CommerceLog.info(
                        "AUTH",
                        "LOGIN_SUCCESS",
                        "User logged successfully",
                        null,
                        Map.of("email", user.getEmail())
                )
        );

        return new AuthResponse(token);
    }
}
