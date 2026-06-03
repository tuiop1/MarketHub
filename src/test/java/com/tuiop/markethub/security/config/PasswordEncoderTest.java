package com.tuiop.markethub.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {

    @Test
    void bcryptEncoder_hashesPasswordAndMatchesRawPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash = encoder.encode("strong-password");

        assertThat(hash).isNotEqualTo("strong-password");
        assertThat(hash).startsWith("$2");
        assertThat(encoder.matches("strong-password", hash)).isTrue();
        assertThat(encoder.matches("wrong-password", hash)).isFalse();
    }
}
