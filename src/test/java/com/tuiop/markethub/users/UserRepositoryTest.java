package com.tuiop.markethub.users;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFlush_appliesEntityDefaultsAndAllowsLookupByEmail() {
        User user = newUser("tymur@example.com");

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEnabled()).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        assertThat(userRepository.existsByEmail("tymur@example.com")).isTrue();
        assertThat(userRepository.findByEmail("tymur@example.com"))
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(savedUser.getId());
    }

    @Test
    void saveAndFlush_whenEmailAlreadyExists_throwsDataIntegrityViolationException() {
        userRepository.saveAndFlush(newUser("duplicate@example.com"));

        User duplicateUser = newUser("duplicate@example.com");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User newUser(String email) {
        return User.builder()
                .firstName("Tymur")
                .lastName("Kurkov")
                .birthDate(LocalDate.of(2004, 5, 12))
                .email(email)
                .passwordHash("bcrypt-hash")
                .build();
    }
}
