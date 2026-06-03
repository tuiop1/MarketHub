package com.tuiop.markethub.security.config;

import com.tuiop.markethub.auth.AuthController;
import com.tuiop.markethub.auth.AuthService;
import com.tuiop.markethub.auth.dto.AuthResponse;
import com.tuiop.markethub.common.GlobalExceptionHandler;
import com.tuiop.markethub.merchants.MerchantAdminController;
import com.tuiop.markethub.merchants.MerchantService;
import com.tuiop.markethub.merchants.dto.MerchantResponse;
import com.tuiop.markethub.orders.OrderController;
import com.tuiop.markethub.orders.OrderService;
import com.tuiop.markethub.products.MerchantProductController;
import com.tuiop.markethub.products.ProductController;
import com.tuiop.markethub.products.ProductService;
import com.tuiop.markethub.products.dto.ProductResponse;
import com.tuiop.markethub.products.images.ProductImageService;
import com.tuiop.markethub.ratelimiter.RateLimitFilter;
import com.tuiop.markethub.security.jwt.AuthEntryPointJwt;
import com.tuiop.markethub.security.jwt.JwtUtil;
import com.tuiop.markethub.security.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                AuthController.class,
                ProductController.class,
                MerchantProductController.class,
                OrderController.class,
                MerchantAdminController.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class),
        excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class,
        properties = {
                "jwt.secret=test-secret-key-test-secret-key-123456",
                "jwt.expiration=3600000"
        }
)
@Import({
        SecurityConfig.class,
        AuthEntryPointJwt.class,
        GlobalExceptionHandler.class,
        SecurityAuthorizationRulesTest.OAuth2ClientTestConfig.class
})
class SecurityAuthorizationRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductImageService productImageService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.tuiop.markethub.auth.oauth2.OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Test
    void authRegister_isPublic() throws Exception {
        given(authService.createUser(any())).willReturn(new AuthResponse("jwt-token", "Bearer", 3_600));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void publicProductList_isPublic() throws Exception {
        given(productService.getPublicProducts(any())).willReturn(Page.empty());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    void merchantProductCreation_whenAnonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/merchant/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductJson(UUID.randomUUID())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/merchant/products"));

        verify(productService, never()).createMyProduct(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void merchantProductCreation_whenAuthenticatedUserIsNotMerchant_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/merchant/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductJson(UUID.randomUUID())))
                .andExpect(status().isForbidden());

        verify(productService, never()).createMyProduct(any(), any());
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void merchantProductCreation_whenAuthenticatedMerchant_returnsCreated() throws Exception {
        UUID categoryId = UUID.randomUUID();
        given(productService.createMyProduct(any(), any())).willReturn(productResponse(categoryId));

        mockMvc.perform(post("/api/v1/merchant/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProductJson(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Keyboard"));

        verify(productService).createMyProduct(any(), any());
    }

    @Test
    void orderPurchase_whenAnonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseJson(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).purchase(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminMerchantVerification_whenAuthenticatedUserIsNotAdmin_returnsForbidden() throws Exception {
        UUID merchantId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/merchants/{merchantId}/verify", merchantId))
                .andExpect(status().isForbidden());

        verify(merchantService, never()).verifyMerchant(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminMerchantVerification_whenAuthenticatedAdmin_returnsOk() throws Exception {
        UUID merchantId = UUID.randomUUID();
        given(merchantService.verifyMerchant(merchantId)).willReturn(new MerchantResponse(
                merchantId,
                UUID.randomUUID(),
                "Tech Shop",
                "Computer hardware",
                true,
                true,
                null
        ));

        mockMvc.perform(patch("/api/v1/admin/merchants/{merchantId}/verify", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        verify(merchantService).verifyMerchant(merchantId);
    }

    private String registerJson() {
        return """
                {
                  "firstName": "Tymur",
                  "lastName": "Kurkov",
                  "birthDate": "2004-05-12",
                  "email": "tymur@example.com",
                  "password": "strong-password"
                }
                """;
    }

    private String createProductJson(UUID categoryId) {
        return """
                {
                  "name": "Keyboard",
                  "description": "Mechanical keyboard",
                  "priceCents": 12999,
                  "stockQuantity": 7,
                  "categoryId": "%s"
                }
                """.formatted(categoryId);
    }

    private String purchaseJson(UUID productId) {
        return """
                {
                  "items": [
                    {
                      "productId": "%s",
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(productId);
    }

    private ProductResponse productResponse(UUID categoryId) {
        return new ProductResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                categoryId,
                "Keyboard",
                "Mechanical keyboard",
                12_999L,
                7,
                true,
                null,
                null,
                List.of()
        );
    }

    @TestConfiguration
    static class OAuth2ClientTestConfig {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .authorizationUri("https://example.com/oauth2/authorize")
                    .tokenUri("https://example.com/oauth2/token")
                    .userInfoUri("https://example.com/oauth2/userinfo")
                    .userNameAttributeName("sub")
                    .scope("openid", "profile", "email")
                    .clientName("Google")
                    .build();

            return new InMemoryClientRegistrationRepository(registration);
        }

        @Bean
        OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }
    }
}
