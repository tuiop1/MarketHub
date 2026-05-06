package com.tuiop.markethub.admin.config;



import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        String email,
        String password,
        String firstName,
        String lastName,
        LocalDate birthDate
) {
    public boolean isConfigured() {
        return email != null && !email.isBlank()
                && password != null && !password.isBlank()
                && firstName != null && !firstName.isBlank()
                && lastName != null && !lastName.isBlank()
                && birthDate != null;
    }
}