package com.tuiop.markethub.products.images;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    @Query("""
    select pi
    from ProductImage pi
    where pi.id = :productImageId
    and pi.product.id = :productId
    and pi.product.merchant.user.id = :userId
    and pi.product.merchant.active = true
    and pi.product.merchant.verified = true
    and pi.product.category.active = true
    and pi.product.active = true
    """)
    Optional<ProductImage> findPubliclyVisibleByIdAndUserIdAndProductId(UUID productImageId, UUID userId, UUID productId);

    @Query("""
    select pi
    from ProductImage pi
    where pi.id = :productImageId
    and pi.product.id = :productId
    and pi.product.merchant.user.id = :userId
    """)
    Optional<ProductImage> findByIdAndUserIdAndProductId(UUID productImageId, UUID userId, UUID productId);



    long countByProductId(UUID productId);

    @Query("""
    select pi
    from ProductImage pi
    join fetch pi.product p
    where p.id in :productIds
    order by pi.position asc, pi.createdAt asc
    """)
    List<ProductImage> findByProductIdIn(@Param("productIds") Collection<UUID> productIds);

    @Query("""
    select pi
    from ProductImage pi
    where
    pi.id =:id
    and pi.product.merchant.active = true
    and pi.product.merchant.verified = true
    and pi.product.category.active = true
    and pi.product.active = true
    """)
    Optional<ProductImage> findPubliclyVisibleById(UUID id);
}
