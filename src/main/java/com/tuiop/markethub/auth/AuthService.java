package com.tuiop.markethub.auth;

import com.tuiop.markethub.auth.dto.AuthResponse;
import com.tuiop.markethub.auth.dto.LoginRequest;
import com.tuiop.markethub.auth.dto.RegisterRequest;
import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.exceptions.EmailAlreadyTakenException;
import com.tuiop.markethub.security.jwt.JwtUtil;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtils;


    @Transactional(readOnly = true)
    public AuthResponse authenticateUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        log.info("User logged in: userId={}",  user.getId());
        return new AuthResponse(jwtUtils.generateToken(user), "Bearer", jwtUtils.getExpirationSeconds());
    }


    @Transactional
    public AuthResponse createUser(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(request.email());
        }
        User newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .email(email)
                .authProvider(AuthProvider.LOCAL)
                .passwordHash(encoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User registered: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        return new AuthResponse(jwtUtils.generateToken(savedUser), "Bearer", jwtUtils.getExpirationSeconds());
    }

}
