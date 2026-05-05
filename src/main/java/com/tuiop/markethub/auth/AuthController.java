package com.tuiop.markethub.security.auth;


import com.tuiop.markethub.security.auth.dto.AuthResponse;
import com.tuiop.markethub.security.auth.dto.LoginRequest;
import com.tuiop.markethub.security.auth.dto.RegisterRequest;
import com.tuiop.markethub.security.jwt.JwtUtil;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtils;
    @PostMapping("/login")

    public AuthResponse authenticateUser(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        User user = userRepository.findByEmail(request.email()).orElseThrow();

        return new AuthResponse(jwtUtils.generateToken(user), "Bearer", jwtUtils.getExpirationSeconds());
    }
    @PostMapping("/register")
    public AuthResponse registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already taken");
        }
        User newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .email(request.email())
                .passwordHash(encoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(newUser);
        return new AuthResponse(jwtUtils.generateToken(savedUser), "Bearer", jwtUtils.getExpirationSeconds());
    }
}
