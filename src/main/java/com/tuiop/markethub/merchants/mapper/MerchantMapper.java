package com.tuiop.markethub.merchants.mapper;

import com.tuiop.markethub.merchants.Merchant;
import com.tuiop.markethub.merchants.dto.MerchantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MerchantMapper {

    @Mapping(target = "userId", source = "user.id")
    MerchantResponse toResponse(Merchant merchant);
}
