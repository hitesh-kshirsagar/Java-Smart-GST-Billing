package com.gst.billing.service;

import com.gst.billing.dto.JwtResponse;
import com.gst.billing.dto.LoginRequest;
import com.gst.billing.dto.RegisterRequest;
import com.gst.billing.entity.User;
import com.gst.billing.repository.UserRepository;
import com.gst.billing.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login with JWT issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user. Defaults role to USER if not provided.
     */
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        String role = (request.getRole() != null && !request.getRole().isBlank())
            ? request.getRole().toUpperCase()
            : "USER";

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();

        userRepository.save(user);
        return "User registered successfully";
    }

    /**
     * Authenticate user credentials and return a signed JWT.
     */
    public JwtResponse login(LoginRequest request) {
        // Throws BadCredentialsException if invalid
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("USER");

        return new JwtResponse(token, userDetails.getUsername(), role);
    }
}
