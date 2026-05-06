package com.tuiop.markethub.admin;


import com.tuiop.markethub.admin.config.AdminProperties;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import com.tuiop.markethub.users.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


//admin initializer, don't create admin if already exists

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final AdminProperties adminProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!adminProperties.isConfigured()) {
            log.info("Admin bootstrap skipped: admin properties are not configured");
            return;
        }

        String email = adminProperties.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            log.info("Admin bootstrap skipped: user already exists with email={}", email);
            return;
        }

        User admin = User.builder()
                .firstName(adminProperties.firstName().trim())
                .lastName(adminProperties.lastName().trim())
                .birthDate(adminProperties.birthDate())
                .email(email)
                .passwordHash(passwordEncoder.encode(adminProperties.password()))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        User savedAdmin = userRepository.save(admin);

        log.warn("Admin user bootstrapped: userId={}, email={}", savedAdmin.getId(), savedAdmin.getEmail());
    }
}