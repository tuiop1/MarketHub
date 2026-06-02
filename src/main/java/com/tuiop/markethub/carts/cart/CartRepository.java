package com.tuiop.markethub.carts.cart;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select distinct c
    from Cart c
    join fetch c.user
    left join fetch c.cartItems ci
    left join fetch ci.product p
    where c.user.id = :userId
    """)
    Optional<Cart> findDetailedByUserIdForUpdate(@Param("userId") UUID userId);

    boolean existsByUserId(UUID id);

    Optional<Cart> findByUserId(UUID userId);
}
