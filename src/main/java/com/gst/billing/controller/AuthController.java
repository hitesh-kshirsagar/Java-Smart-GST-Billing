package com.gst.billing.controller;

import com.gst.billing.dto.JwtResponse;
import com.gst.billing.dto.LoginRequest;
import com.gst.billing.dto.RegisterRequest;
import com.gst.billing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for user registration and JWT login.
 *
 * POST /api/auth/register  — create new account
 * POST /api/auth/login     — returns JWT token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /** Register a new user account */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /** Authenticate and receive a JWT */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
