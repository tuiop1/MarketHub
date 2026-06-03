package com.tuiop.markethub.merchants;

import com.tuiop.markethub.merchants.dto.CreateMerchantRequest;
import com.tuiop.markethub.merchants.dto.MerchantResponse;
import com.tuiop.markethub.merchants.mapper.MerchantMapper;
import com.tuiop.markethub.security.user.CustomUserDetails;
import com.tuiop.markethub.users.AuthProvider;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import com.tuiop.markethub.users.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private CustomUserDetails principal;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    void createMerchant_whenRequestIsValid_trimsShopNameSavesMerchantAndPromotesUserRole() {
        UUID userId = UUID.randomUUID();
        User user = user(userId, UserRole.USER);
        CreateMerchantRequest request = new CreateMerchantRequest("  Tech Shop  ", "Computer hardware");
        MerchantResponse mappedResponse = new MerchantResponse(
                UUID.randomUUID(),
                userId,
                "Tech Shop",
                "Computer hardware",
                false,
                true,
                null
        );

        given(principal.getUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(merchantRepository.existsByShopNameIgnoreCase("Tech Shop")).willReturn(false);
        given(merchantRepository.existsByUserId(userId)).willReturn(false);
        given(merchantRepository.save(any(Merchant.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(merchantMapper.toResponse(any(Merchant.class))).willReturn(mappedResponse);

        MerchantResponse response = merchantService.createMerchant(principal, request);

        assertThat(response).isSameAs(mappedResponse);
        assertThat(user.getRole()).isEqualTo(UserRole.MERCHANT);

        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantRepository).save(merchantCaptor.capture());

        Merchant savedMerchant = merchantCaptor.getValue();
        assertThat(savedMerchant.getUser()).isSameAs(user);
        assertThat(savedMerchant.getShopName()).isEqualTo("Tech Shop");
        assertThat(savedMerchant.getDescription()).isEqualTo("Computer hardware");
    }

    @Test
    void verifyMerchant_whenMerchantExists_marksMerchantAsVerifiedAndMapsResponse() {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Merchant merchant = Merchant.builder()
                .id(merchantId)
                .user(user(userId, UserRole.MERCHANT))
                .shopName("Tech Shop")
                .description("Computer hardware")
                .verified(false)
                .active(true)
                .build();
        MerchantResponse mappedResponse = new MerchantResponse(
                merchantId,
                userId,
                "Tech Shop",
                "Computer hardware",
                true,
                true,
                null
        );

        given(merchantRepository.findById(merchantId)).willReturn(Optional.of(merchant));
        given(merchantMapper.toResponse(merchant)).willReturn(mappedResponse);

        MerchantResponse response = merchantService.verifyMerchant(merchantId);

        assertThat(response).isSameAs(mappedResponse);
        assertThat(merchant.getVerified()).isTrue();
        verify(merchantMapper).toResponse(merchant);
    }

    private User user(UUID userId, UserRole role) {
        return User.builder()
                .id(userId)
                .firstName("Tymur")
                .lastName("Kurkov")
                .birthDate(LocalDate.of(2004, 5, 12))
                .email("user-" + userId + "@example.com")
                .passwordHash("bcrypt-hash")
                .role(role)
                .enabled(true)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
}
