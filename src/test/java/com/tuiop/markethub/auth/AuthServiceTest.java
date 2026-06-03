package com.tuiop.markethub.auth;

import com.tuiop.markethub.auth.dto.AuthResponse;
import com.tuiop.markethub.auth.dto.LoginRequest;
import com.tuiop.markethub.auth.dto.RegisterRequest;
import com.tuiop.markethub.security.jwt.JwtUtil;
import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import com.tuiop.markethub.users.UserRole;
import com.tuiop.markethub.users.exceptions.EmailAlreadyTakenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long TOKEN_EXPIRATION_SECONDS = 3_600L;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void createUser_whenEmailIsAvailable_savesLocalUserWithNormalizedEmailAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(
                "Tymur",
                "Kurkov",
                LocalDate.of(2004, 5, 12),
                "  TYMuR@example.COM ",
                "plain-password"
        );
        User persistedUser = persistedUser("tymur@example.com");

        given(userRepository.existsByEmail("tymur@example.com")).willReturn(false);
        given(passwordEncoder.encode("plain-password")).willReturn("bcrypt-hash");
        given(userRepository.save(any(User.class))).willReturn(persistedUser);
        given(jwtUtil.generateToken(persistedUser)).willReturn("jwt-token");
        given(jwtUtil.getExpirationSeconds()).willReturn(TOKEN_EXPIRATION_SECONDS);

        AuthResponse response = authService.createUser(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(TOKEN_EXPIRATION_SECONDS);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertThat(userToSave.getEmail()).isEqualTo("tymur@example.com");
        assertThat(userToSave.getFirstName()).isEqualTo("Tymur");
        assertThat(userToSave.getLastName()).isEqualTo("Kurkov");
        assertThat(userToSave.getBirthDate()).isEqualTo(LocalDate.of(2004, 5, 12));
        assertThat(userToSave.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(userToSave.getPasswordHash()).isNotEqualTo("plain-password");
        assertThat(userToSave.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    void createUser_whenEmailAlreadyExists_throwsConflictAndDoesNotSaveUser() {
        RegisterRequest request = new RegisterRequest(
                "Tymur",
                "Kurkov",
                LocalDate.of(2004, 5, 12),
                "taken@example.com",
                "plain-password"
        );

        given(userRepository.existsByEmail("taken@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.createUser(request))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessageContaining("taken@example.com")
                .hasMessageContaining("already taken");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void authenticateUser_whenCredentialsAreValid_authenticatesByNormalizedEmailAndReturnsToken() {
        LoginRequest request = new LoginRequest("  TYMuR@example.COM ", "plain-password");
        User user = persistedUser("tymur@example.com");

        given(authenticationManager.authenticate(any(Authentication.class)))
                .willReturn(new UsernamePasswordAuthenticationToken("tymur@example.com", null));
        given(userRepository.findByEmail("tymur@example.com")).willReturn(Optional.of(user));
        given(jwtUtil.generateToken(user)).willReturn("jwt-token");
        given(jwtUtil.getExpirationSeconds()).willReturn(TOKEN_EXPIRATION_SECONDS);

        AuthResponse response = authService.authenticateUser(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(TOKEN_EXPIRATION_SECONDS);

        ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());

        Authentication authentication = authenticationCaptor.getValue();
        assertThat(authentication.getPrincipal()).isEqualTo("tymur@example.com");
        assertThat(authentication.getCredentials()).isEqualTo("plain-password");
    }

    private User persistedUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Tymur")
                .lastName("Kurkov")
                .birthDate(LocalDate.of(2004, 5, 12))
                .email(email)
                .passwordHash("bcrypt-hash")
                .role(UserRole.USER)
                .enabled(true)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
}
