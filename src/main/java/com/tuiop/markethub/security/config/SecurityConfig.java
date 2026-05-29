package com.tuiop.markethub.security.config;

import com.tuiop.markethub.auth.oauth2.OAuth2LoginSuccessHandler;
import com.tuiop.markethub.security.jwt.AuthEntryPointJwt;
import com.tuiop.markethub.security.jwt.AuthTokenFilter;
import com.tuiop.markethub.security.user.CustomUserDetailsService;
import com.tuiop.markethub.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthEntryPointJwt unauthorizedHandler;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        return new AuthTokenFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/merchants").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/product-images/**").permitAll()
                                .requestMatchers("/api/v1/merchant/products/**").hasRole("MERCHANT")
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()

                ).oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler));
        http.addFilterBefore(authenticationJwtTokenFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
