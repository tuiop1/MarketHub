package com.tuiop.markethub.merchants;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Page<Merchant> findByActiveTrueAndVerifiedFalse(Pageable pageable);

    boolean existsByShopNameIgnoreCase(String shopName);

    boolean existsByUserId(UUID id);

    Optional<Merchant> findByUserId(UUID id);

    Page<Merchant> findByActiveTrueAndVerifiedTrue(Pageable pageable);
}
