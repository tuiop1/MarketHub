package com.tuiop.markethub.products;


import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {


    @EntityGraph(attributePaths = {"merchant", "category"})
    Optional<Product> findWithMerchantAndCategoryById(UUID id);

    @EntityGraph(attributePaths = {"merchant", "category", "images"})
    @Query("""
    select p
    from Product p
    where p.id = :productId
      and p.active = true
      and p.merchant.active = true
      and p.merchant.verified = true
      and p.category.active = true
    """)
    Optional<Product> findPublicByIdWithImages(@Param("productId") UUID productId);

    @EntityGraph(attributePaths = {"merchant", "category"})
    Page<Product> findByMerchantId(UUID merchantId, Pageable pageable);

    @EntityGraph(attributePaths = {"merchant", "category", "images"})
    Optional<Product> findWithImagesByIdAndMerchantId(UUID productId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findByIdAndMerchantId(UUID productId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p
    from Product p
    where p.id = :productId
    and p.active = true
    and p.merchant.active = true
    and p.merchant.verified = true
    and p.category.active = true
    and p.merchant.user.id = :userId
    """)
    Optional<Product> findByIdAndUserIdForUpdate(UUID productId, UUID userId);

    @EntityGraph(attributePaths = {"merchant", "category"})
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
