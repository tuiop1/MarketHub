package com.tuiop.markethub.security.jwt;

import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-test-secret-key-123456";
    private static final int EXPIRATION_MS = 3_600_000;

    @Test
    void generateToken_containsUserIdentityClaimsAndCanBeValidated() {
        JwtUtil jwtUtil = jwtUtil();
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .firstName("Tymur")
                .lastName("Kurkov")
                .birthDate(LocalDate.of(2004, 5, 12))
                .email("tymur@example.com")
                .passwordHash("bcrypt-hash")
                .role(UserRole.MERCHANT)
                .enabled(true)
                .authProvider(AuthProvider.LOCAL)
                .build();

        String token = jwtUtil.generateToken(user);

        Claims claims = parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getIssuer()).isEqualTo("auth-service");
        assertThat(claims.get("email", String.class)).isEqualTo("tymur@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("MERCHANT");
        assertThat(claims.getExpiration()).isAfter(new Date());
        assertThat(jwtUtil.getExpirationSeconds()).isEqualTo(3_600L);
        assertThat(jwtUtil.getUserIdFromValidToken(token)).contains(userId);
    }

    @Test
    void getUserIdFromValidToken_whenTokenIsInvalid_returnsEmptyOptional() {
        JwtUtil jwtUtil = jwtUtil();

        assertThat(jwtUtil.getUserIdFromValidToken("not-a-jwt")).isEmpty();
    }

    private JwtUtil jwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", EXPIRATION_MS);
        jwtUtil.init();
        return jwtUtil;
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
