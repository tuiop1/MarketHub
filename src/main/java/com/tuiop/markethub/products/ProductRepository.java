package com.tuiop.markethub.products;


import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByMerchantId(UUID merchantId, Pageable pageable);

    Optional<Product> findByIdAndMerchantId(UUID productId, UUID merchantId);


    Page<Product> findByActiveTrueAndMerchantActiveTrueAndMerchantVerifiedTrueAndCategoryActiveTrue(Pageable pageable);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p from Product p
        join fetch p.merchant m
        join fetch m.user
        join fetch p.category c
        where p.id in :productIds
          and p.active = true
          and m.active = true
          and m.verified = true
          and c.active = true
        """)
    List<Product> findBuyableByIdsForUpdate(@Param("productIds") Collection<UUID> productIds);
}