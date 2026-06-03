package com.tuiop.markethub.auth;

import com.tuiop.markethub.auth.dto.AuthResponse;
import com.tuiop.markethub.auth.dto.LoginRequest;
import com.tuiop.markethub.auth.dto.RegisterRequest;
import com.tuiop.markethub.common.GlobalExceptionHandler;
import com.tuiop.markethub.ratelimiter.RateLimitFilter;
import com.tuiop.markethub.users.exceptions.EmailAlreadyTakenException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class),
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerUser_whenRequestIsValid_returnsAuthResponse() throws Exception {
        given(authService.createUser(any(RegisterRequest.class)))
                .willReturn(new AuthResponse("jwt-token", "Bearer", 3_600));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Tymur",
                                  "lastName": "Kurkov",
                                  "birthDate": "2004-05-12",
                                  "email": "tymur@example.com",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3_600));

        ArgumentCaptor<RegisterRequest> requestCaptor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).createUser(requestCaptor.capture());

        RegisterRequest request = requestCaptor.getValue();
        assertThat(request.firstName()).isEqualTo("Tymur");
        assertThat(request.lastName()).isEqualTo("Kurkov");
        assertThat(request.birthDate()).isEqualTo(LocalDate.of(2004, 5, 12));
        assertThat(request.email()).isEqualTo("tymur@example.com");
        assertThat(request.password()).isEqualTo("strong-password");
    }

    @Test
    void loginUser_whenRequestIsValid_returnsAuthResponse() throws Exception {
        given(authService.authenticateUser(any(LoginRequest.class)))
                .willReturn(new AuthResponse("jwt-token", "Bearer", 3_600));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "tymur@example.com",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3_600));

        ArgumentCaptor<LoginRequest> requestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).authenticateUser(requestCaptor.capture());

        LoginRequest request = requestCaptor.getValue();
        assertThat(request.email()).isEqualTo("tymur@example.com");
        assertThat(request.password()).isEqualTo("strong-password");
    }

    @Test
    void registerUser_whenEmailIsInvalid_returnsBadRequestAndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Tymur",
                                  "lastName": "Kurkov",
                                  "birthDate": "2004-05-12",
                                  "email": "not-an-email",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("email")))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));

        verifyNoInteractions(authService);
    }

    @Test
    void registerUser_whenEmailAlreadyExists_returnsConflict() throws Exception {
        given(authService.createUser(any(RegisterRequest.class)))
                .willThrow(new EmailAlreadyTakenException("tymur@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Tymur",
                                  "lastName": "Kurkov",
                                  "birthDate": "2004-05-12",
                                  "email": "tymur@example.com",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already taken")))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));
    }
}
