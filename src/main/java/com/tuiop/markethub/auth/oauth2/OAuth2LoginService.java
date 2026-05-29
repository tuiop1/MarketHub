package com.tuiop.markethub.auth.oauth2;

import com.tuiop.markethub.auth.dto.AuthResponse;
import com.tuiop.markethub.security.jwt.JwtUtil;
import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import com.tuiop.markethub.users.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse loginWithGoogle(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        if (!"google".equals(oauthToken.getAuthorizedClientRegistrationId())) {
            throw new BadCredentialsException("Unsupported OAuth provider");
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();

        String providerId = getRequiredAttribute(oauthUser, "sub");
        String email = getRequiredAttribute(oauthUser, "email").trim().toLowerCase();

        Boolean emailVerified = oauthUser.getAttribute("email_verified");
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new BadCredentialsException("Google email is not verified");
        }

        User user = userRepository
                .findByAuthProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                .orElseGet(() -> findExistingUserOrCreateGoogleUser(oauthUser, email, providerId));

        return new AuthResponse(
                jwtUtil.generateToken(user),
                "Bearer",
                jwtUtil.getExpirationSeconds()
        );
    }

    private User findExistingUserOrCreateGoogleUser(
            OAuth2User oauthUser,
            String email,
            String providerId
    ) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(oauthUser, email, providerId));
    }

    private User createGoogleUser(OAuth2User oauthUser, String email, String providerId) {
        User user = User.builder()
                .email(email)
                .firstName(oauthUser.getAttribute("given_name"))
                .lastName(oauthUser.getAttribute("family_name"))
                .role(UserRole.USER)
                .enabled(true)
                .authProvider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .build();

        return userRepository.save(user);
    }

    private String getRequiredAttribute(OAuth2User oauthUser, String name) {
        Object value = oauthUser.getAttribute(name);

        if (value == null || value.toString().isBlank()) {
            throw new BadCredentialsException("Missing OAuth attribute: " + name);
        }

        return value.toString();
    }
}