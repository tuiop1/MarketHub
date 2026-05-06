package com.tuiop.markethub.auth;


import com.tuiop.markethub.auth.dto.AuthResponse;
import com.tuiop.markethub.auth.dto.LoginRequest;
import com.tuiop.markethub.auth.dto.RegisterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public AuthResponse loginUser(@Valid @RequestBody LoginRequest request) {
        return authService.authenticateUser(request);
    }

    @PostMapping("/register")
    public AuthResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        return authService.createUser(request);
    }

}
